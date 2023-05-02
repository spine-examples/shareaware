package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.ReplenishmentOperationId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.WithdrawalOperationId;
import io.spine.examples.shareaware.given.GivenMoney;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections.MoneyCannotBeTransferredFromUser;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletReplenishment;
import io.spine.examples.shareaware.wallet.WalletWithdrawal;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.event.MoneyNotWithdrawn;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.event.WalletNotReplenished;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.examples.shareaware.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.MoneyCalculator.sum;
import static io.spine.examples.shareaware.given.GivenMoney.moneyOf;

public final class WalletTestEnv {

    /**
     * Prevents instantiation of this test environment.
     */
    private WalletTestEnv() {
    }

    private static final Iban IBAN = Iban
            .newBuilder()
            .setValue("FI211234569876543210")
            .vBuild();

    public static WalletId givenId() {
        return WalletId
                .newBuilder()
                .setOwner(GivenUserId.generated())
                .vBuild();
    }

    public static CreateWallet createWallet(WalletId id) {
        return CreateWallet
                .newBuilder()
                .setWallet(id)
                .vBuild();
    }

    /**
     * Generates {@code ReplenishWallet} command.
     */
    private static ReplenishWallet replenish(WalletId wallet, Money amount) {
        var replenishment = ReplenishmentId.generate();
        return ReplenishWallet
                .newBuilder()
                .setWallet(wallet)
                .setReplenishment(replenishment)
                .setIban(IBAN)
                .setMoneyAmount(amount)
                .vBuild();
    }

    /**
     * Generates {@code ReplenishWallet} command on 500 USD for the wallet.
     */
    public static ReplenishWallet replenish(WalletId wallet) {
        var replenishmentAmount = moneyOf(500, Currency.USD);
        return replenish(wallet, replenishmentAmount);
    }

    /**
     * Creates a {@code Wallet} in {@code context} by sending {@code CreateWallet} command to it.
     *
     * @return the ID of created {@code Wallet}.
     */
    public static WalletId setUpWallet(BlackBoxContext context) {
        var wallet = givenId();
        var command = createWallet(wallet);
        context.receivesCommand(command);
        return wallet;
    }

    public static Wallet walletReplenishedBy(ReplenishWallet firstReplenishment,
                                             ReplenishWallet secondReplenishment,
                                             WalletId wallet) {
        var expectedBalance = sum(firstReplenishment.getMoneyAmount(),
                                  secondReplenishment.getMoneyAmount());
        return Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static BalanceRecharged balanceRechargedBy(ReplenishWallet command, WalletId wallet) {
        return BalanceRecharged
                .newBuilder()
                .setWallet(wallet)
                .setCurrentBalance(command.getMoneyAmount())
                .setOperation(operationId(command))
                .vBuild();
    }

    public static WalletBalance walletBalanceAfterReplenishment(ReplenishWallet firstReplenishment,
                                                                ReplenishWallet secondReplenishment,
                                                                WalletId wallet) {
        var replenishedWallet = walletReplenishedBy(firstReplenishment,
                                                    secondReplenishment,
                                                    wallet);
        return WalletBalance
                .newBuilder()
                .setId(replenishedWallet.getId())
                .setBalance(replenishedWallet.getBalance())
                .vBuild();
    }

    public static WalletReplenishment walletReplenishmentBy(ReplenishWallet command) {
        return WalletReplenishment
                .newBuilder()
                .setWallet(command.getWallet())
                .setId(command.getReplenishment())
                .setAmount(command.getMoneyAmount())
                .vBuild();
    }

    public static TransferMoneyFromUser transferMoneyFromUserBy(ReplenishWallet command,
                                                                Iban recipient) {
        return TransferMoneyFromUser
                .newBuilder()
                .setGateway(PaymentGatewayProcess.ID)
                .setReplenishmentProcess(command.getReplenishment())
                .setAmount(command.getMoneyAmount())
                .setSender(command.getIban())
                .setRecipient(recipient)
                .vBuild();
    }

    public static RechargeBalance rechargeBalanceWhen(ReplenishWallet command) {
        return RechargeBalance
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command))
                .setMoneyAmount(command.getMoneyAmount())
                .vBuild();
    }

    public static WalletReplenished walletReplenishedAfter(ReplenishWallet command) {
        return WalletReplenished
                .newBuilder()
                .setReplenishment(command.getReplenishment())
                .setWallet(command.getWallet())
                .setMoneyAmount(command.getMoneyAmount())
                .vBuild();
    }

    public static MoneyCannotBeTransferredFromUser
    moneyCannotBeTransferredFromUserBy(ReplenishmentId replenishmentProcess) {
        return MoneyCannotBeTransferredFromUser
                .newBuilder()
                .setReplenishment(replenishmentProcess)
                .vBuild();
    }

    public static WalletNotReplenished
    walletNotReplenishedAfter(ReplenishWallet command) {
        return WalletNotReplenished
                .newBuilder()
                .setReplenishment(command.getReplenishment())
                .vBuild();
    }

    public static Wallet setUpReplenishedWallet(BlackBoxContext context) {
        var wallet = setUpWallet(context);
        var command = replenish(wallet);
        context.receivesCommand(command);
        return Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(command.getMoneyAmount())
                .vBuild();
    }

    public static WithdrawMoney withdrawMoneyFrom(WalletId wallet) {
        return WithdrawMoney
                .newBuilder()
                .setWithdrawalProcess(WithdrawalId.generate())
                .setWallet(wallet)
                .setRecipient(IBAN)
                .setAmount(moneyOf(200, Currency.USD))
                .vBuild();
    }

    private static WithdrawalOperationId operationId(WithdrawalId withdrawal) {
        return WithdrawalOperationId
                .newBuilder()
                .setWithdrawal(withdrawal)
                .vBuild();
    }

    public static ReservedMoneyDebited reservedMoneyDebitedFrom(Wallet wallet,
                                                                WithdrawMoney command) {
        var reducedBalance = subtract(wallet.getBalance(), command.getAmount());
        return ReservedMoneyDebited
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(wallet.getId())
                .setCurrentBalance(reducedBalance)
                .vBuild();
    }

    public static MoneyReserved moneyReservedBy(WithdrawMoney command) {
        return MoneyReserved
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static InsufficientFunds insufficientFundsIn(WalletId wallet, WithdrawMoney command) {
        return InsufficientFunds
                .newBuilder()
                .setWallet(wallet)
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static MoneyReservationCanceled moneyReservationCanceledBy(WithdrawMoney command) {
        return MoneyReservationCanceled
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command.getWithdrawalProcess()))
                .vBuild();
    }

    public static WalletWithdrawal walletWithdrawalBy(WithdrawMoney command) {
        return WalletWithdrawal
                .newBuilder()
                .setId(command.getWithdrawalProcess())
                .setWallet(command.getWallet())
                .setRecipient(command.getRecipient())
                .vBuild();
    }

    public static ReserveMoney reserveMoneyWith(WithdrawMoney command) {
        return ReserveMoney
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static TransferMoneyToUser transferMoneyToUserWith(WithdrawMoney command,
                                                              Iban sender) {
        return TransferMoneyToUser
                .newBuilder()
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setGateway(PaymentGatewayProcess.ID)
                .setSender(sender)
                .setRecipient(command.getRecipient())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static DebitReservedMoney debitReservedMoneyWith(WithdrawMoney command) {
        return DebitReservedMoney
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .vBuild();
    }

    public static MoneyWithdrawn moneyWithdrawnBy(WithdrawMoney command, Wallet wallet) {
        var reducedBalance = subtract(wallet.getBalance(), command.getAmount());
        return MoneyWithdrawn
                .newBuilder()
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setWallet(command.getWallet())
                .setCurrentBalance(reducedBalance)
                .vBuild();
    }

    public static MoneyNotWithdrawn moneyNotWithdrawnBy(WithdrawalId withdrawalProcess) {
        return MoneyNotWithdrawn
                .newBuilder()
                .setWithdrawalProcess(withdrawalProcess)
                .vBuild();
    }

    public static CancelMoneyReservation cancelMoneyReservationBy(WithdrawMoney command) {
        return CancelMoneyReservation
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command.getWithdrawalProcess()))
                .vBuild();
    }

    public static MoneyTransferredToUser moneyTransferredToUserBy(WithdrawMoney command) {
        return MoneyTransferredToUser
                .newBuilder()
                .setGetaway(PaymentGatewayProcess.ID)
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static Wallet walletWhichWasWithdrawnBy(WithdrawMoney firstWithdraw,
                                                   WithdrawMoney secondWithdraw,
                                                   Wallet wallet) {
        var withdrawalAmount = sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        var expectedBalance = subtract(wallet.getBalance(), withdrawalAmount);
        return Wallet
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static WalletBalance walletBalanceReducedBy(WithdrawMoney firstWithdraw,
                                                       WithdrawMoney secondWithdraw,
                                                       Wallet wallet) {
        var withdrawalAmount = sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        var expectedBalance = subtract(wallet.getBalance(), withdrawalAmount);
        return WalletBalance
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static Wallet walletWith(Money balance, WalletId id) {
        return Wallet
                .newBuilder()
                .setBalance(balance)
                .setId(id)
                .vBuild();
    }

    public static WalletCreated walletCreatedWith(Money initialBalance, WalletId id) {
        return WalletCreated
                .newBuilder()
                .setWallet(id)
                .setBalance(initialBalance)
                .vBuild();
    }

    public static WalletBalance zeroWalletBalance(WalletId id) {
        return WalletBalance
                .newBuilder()
                .setId(id)
                .setBalance(GivenMoney.zero())
                .vBuild();
    }

    private static ReplenishmentOperationId operationId(ReplenishWallet c) {
        return ReplenishmentOperationId
                .newBuilder()
                .setReplenishment(c.getReplenishment())
                .vBuild();
    }
}
