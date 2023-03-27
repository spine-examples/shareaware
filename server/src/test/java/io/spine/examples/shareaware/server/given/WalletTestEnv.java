package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.OperationId;
import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletWithdrawal;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyNotWithdrawn;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.examples.shareaware.server.given.GivenMoney.moneyOf;
import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.sum;

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
    public static ReplenishWallet replenish(WalletId wallet, Money amount) {
        ReplenishmentId replenishment = ReplenishmentId.generate();
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
        Money replenishmentAmount = moneyOf(500, Currency.USD);
        return replenish(wallet, replenishmentAmount);
    }

    /**
     * Creates a {@code Wallet} in {@code context} by sending {@code CreateWallet} command to it.
     *
     * @return the ID of created {@code Wallet}.
     */
    public static WalletId setUpWallet(BlackBoxContext context) {
        WalletId wallet = givenId();
        CreateWallet command = createWallet(wallet);
        context.receivesCommand(command);
        return wallet;
    }

    public static Wallet setUpReplenishedWallet(BlackBoxContext context) {
        WalletId wallet = setUpWallet(context);
        ReplenishWallet command = replenish(wallet);
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

    private static OperationId operationId(WithdrawalId withdrawal) {
        return OperationId
                .newBuilder()
                .setWithdrawal(withdrawal)
                .vBuild();
    }

    public static ReservedMoneyDebited reservedMoneyDebitedFrom(Wallet wallet,
                                                                WithdrawMoney command) {
        Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
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
        Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
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
        Money withdrawalAmount =
                sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        Money expectedBalance =
                subtract(wallet.getBalance(), withdrawalAmount);
        return Wallet
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static WalletBalance walletBalanceReducedBy(WithdrawMoney firstWithdraw,
                                                       WithdrawMoney secondWithdraw,
                                                       Wallet wallet) {
        Money withdrawalAmount =
                sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        Money expectedBalance =
                subtract(wallet.getBalance(), withdrawalAmount);
        return WalletBalance
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }
}
