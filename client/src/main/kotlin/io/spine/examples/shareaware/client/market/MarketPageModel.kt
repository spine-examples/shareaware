/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.shareaware.client.market

import com.google.common.base.Preconditions
import io.spine.client.EventFilter
import io.spine.examples.shareaware.PurchaseId
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.client.DesktopClient
import io.spine.examples.shareaware.client.EntitySubscription
import io.spine.examples.shareaware.investment.command.PurchaseShares
import io.spine.examples.shareaware.investment.event.SharesPurchased
import io.spine.examples.shareaware.market.AvailableMarketShares
import io.spine.examples.shareaware.server.market.MarketProcess
import io.spine.examples.shareaware.share.Share
import io.spine.examples.shareaware.wallet.rejection.Rejections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI model for the `MarketPage`.
 */
public class MarketPageModel(client: DesktopClient) {
    private val previousShares: MutableStateFlow<AvailableMarketShares?> = MutableStateFlow(null)
    private val sharesSubscriptions: EntitySubscription<AvailableMarketShares> =
        EntitySubscription(
            AvailableMarketShares::class.java, client, MarketProcess.ID
        ) { state -> previousShares.value = state }
    private val selectedShareId: MutableStateFlow<ShareId?> = MutableStateFlow(null)
    public val purchaseOperationModel: PurchaseOperationModel = PurchaseOperationModel(client)

    /**
     * Returns the current state of available shares on the market.
     *
     * The returnable value may contain null when the market closed or
     * when an error occurred in the market system.
     */
    public fun shares(): StateFlow<AvailableMarketShares?> {
        return sharesSubscriptions.state()
    }

    /**
     * Returns the available shares on the market in the state
     * they were before the previous market update.
     */
    public fun previousShares(): StateFlow<AvailableMarketShares?> {
        return previousShares
    }

    /**
     * Sets the share that the user wants to interact with.
     */
    public fun selectedShare(share: ShareId) {
        selectedShareId.value = share
    }

    /**
     * Returns the share user selected for interaction.
     */
    public fun selectedShare(): StateFlow<ShareId?> {
        return selectedShareId
    }

    /**
     * Controls the state of the shares purchase operation.
     */
    public class PurchaseOperationModel(private val client: DesktopClient) {
        private val inProgressState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private val shareState: MutableStateFlow<Share?> = MutableStateFlow(null)
        private val quantityState: MutableStateFlow<Int> = MutableStateFlow(0)
        private val resultMessageShownState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private val resultMessageState: MutableStateFlow<String> = MutableStateFlow("")
        private val isFailedState: MutableStateFlow<Boolean> = MutableStateFlow(false)

        /**
         * Initiates the purchase.
         *
         * @param share the share to purchase
         */
        public fun initiate(share: Share) {
            inProgressState.value = true
            shareState.value = share
        }

        /**
         * Returns the state of the purchase operation is it in progress or not.
         */
        public fun isInProgress(): StateFlow<Boolean> {
            return inProgressState
        }

        /**
         * Sends the `PurchaseShares` command to the server.
         */
        public fun complete() {
            val share = shareState.value
            Preconditions.checkNotNull(share)
            val purchaseShares = PurchaseShares
                .newBuilder()
                .buildWith(share!!)
            val purchaseProcess = purchaseShares.purchaseProcess
            subscribeToSharesPurchased(purchaseProcess)
            subscribeToInsufficientFunds(purchaseProcess)
            subscribeToSharesCannotBeObtained(purchaseProcess)
            client.command(purchaseShares)
        }

        /**
         * Cancels the purchase operation.
         */
        public fun cancel() {
            inProgressState.value = false
            quantityState.value = 0
        }

        /**
         * Returns the share user wants to purchase.
         */
        public fun share(): StateFlow<Share?> {
            return shareState
        }

        /**
         * Returns the quantity of shares user wants to purchase.
         */
        public fun quantityOfShares(): StateFlow<Int> {
            return quantityState
        }

        /**
         * Sets the quantity of shares user wants to purchase.
         */
        public fun quantityOfShares(quantity: Int) {
            quantityState.value = quantity
        }

        /**
         * Returns the state of the message about the purchase operation result visibility.
         */
        public fun isResultMessageShown(): StateFlow<Boolean> {
            return resultMessageShownState
        }

        /**
         * Returns the current state of the message that signals about the purchase operation result.
         */
        public fun resultMessage(): StateFlow<String> {
            return resultMessageState
        }

        /**
         * Returns the current state of the purchase operation, whether it failed or not.
         */
        public fun isFailed(): StateFlow<Boolean> {
            return isFailedState
        }

        /**
         * Enables the visibility of the message about successful purchase operation
         * and sets the provided message to it.
         */
        private fun showSuccessfulOperationMessage(message: String) {
            resultMessageShownState.value = true
            isFailedState.value = false
            resultMessageState.value = message
        }

        /**
         * Enables the visibility of the message about failed purchase operation
         * and sets the provided message to it.
         */
        private fun showFailedOperationMessage(message: String) {
            resultMessageShownState.value = true
            isFailedState.value = true
            resultMessageState.value = message
        }

        /**
         * Disables the visibility of the message about purchase operation result
         * and clears its message.
         */
        public fun closeOperationResultMessage() {
            resultMessageShownState.value = false
            isFailedState.value = false
            resultMessageState.value = ""
        }

        /**
         * Subscribes to the `SharesPurchased` event.
         */
        private fun subscribeToSharesPurchased(id: PurchaseId) {
            val purchaseIdField = SharesPurchased.Field.purchaseProcess()
            client.subscribeOnce(
                SharesPurchased::class.java,
                EventFilter.eq(purchaseIdField, id)
            ) {
                showSuccessfulOperationMessage("Shares have been purchased successfully.")
            }
        }

        /**
         * Subscribes to the `InsufficientFunds` event, which signals that the price of purchase
         * exceeds the amount of available funds on the balance.
         */
        private fun subscribeToInsufficientFunds(id: PurchaseId) {
            val purchaseIdField = Rejections.InsufficientFunds.Field.operation().purchase()
            client.subscribeOnce(
                Rejections.InsufficientFunds::class.java,
                EventFilter.eq(purchaseIdField, id)
            ) {
                showFailedOperationMessage(
                    "There are insufficient funds on your balance to purchase those shares."
                )
            }
        }

        /**
         * Subscribes to the `SharesCannotBeObtained` event that signals about error in the market,
         * when trying to purchase shares.
         */
        private fun subscribeToSharesCannotBeObtained(id: PurchaseId) {
            val purchaseIdField = io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained.Field.purchaseProcess()
            client.subscribeOnce(
                io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained::class.java,
                EventFilter.eq(purchaseIdField, id)
            ) {
                showFailedOperationMessage(
                    "An error occurred in the market system while shares were purchasing."
                )
            }
        }

        /**
         * Returns the command to purchase shares.
         *
         * @param share the share to purchase
         */
        private fun PurchaseShares.Builder.buildWith(share: Share): PurchaseShares {
            return this
                .setPurchaseProcess(PurchaseId.generate())
                .setPurchaser(client.authenticatedUser())
                .setPrice(share.price)
                .setQuantity(quantityState.value)
                .setShare(share.id)
                .vBuild()
        }
    }
}
