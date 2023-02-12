package com.xpl0t.scany.ui.viewsubscription

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingFlowParams
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.add
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.models.SubscriptionOffer
import com.xpl0t.scany.services.BillingService
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.reorderpages.ReorderPagesFragment
import com.xpl0t.scany.util.Optional
import com.xpl0t.scany.views.FailedCard
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject


@AndroidEntryPoint
class ViewSubscriptionFragment : BaseFragment(R.layout.view_subscription) {

    private val args: ViewSubscriptionFragmentArgs by navArgs()

    @Inject
    lateinit var billingSv: BillingService

    private val getSubOffersTrigger = BehaviorSubject.createDefault(0)

    private lateinit var costOverviewTextBox: TextView
    private lateinit var benefitsList: RecyclerView
    private val benefitsAdapter = BenefitAdapter()
    private lateinit var subOptionsContainer: View
    private lateinit var periodChipGroup: ChipGroup
    private lateinit var failedCard: FailedCard

    private var subOffers: List<SubscriptionOffer>? = null

    private var disposable: Disposable? = null
    private var disposables = mutableListOf<Disposable>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        disposable = getSubscriptionOffers().subscribe {
            Log.i(TAG, "Got subscription offers")
            subOffers = it

            runOnUiThread {
                val preselectedOffer = it.first()
                periodChipGroup.check(preselectedOffer.chipId)
                costOverviewTextBox.text = resources.getString(
                    preselectedOffer.costOverviewResId, preselectedOffer.formattedPrice
                )

                failedCard.visibility = View.GONE
                subOptionsContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        disposables.add {
            billingSv.isSubscribedObs
                .filter { it }
                .subscribe {
                    finish()
                }
        }

        benefitsAdapter.updateItems(
            resources.getStringArray(R.array.view_sub_benefits).toList()
        )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
        disposables.forEach { it.dispose() }
    }

    private fun initViews() {
        val reasonTextView = requireView().findViewById<TextView>(R.id.reason)
        reasonTextView.text = args.reasonText

        val clearBtn = requireView().findViewById<ImageView>(R.id.clear)
        clearBtn.setOnClickListener {
            Log.i(TAG, "Clear button clicked")
            finish()
        }

        costOverviewTextBox = requireView().findViewById(R.id.cost_overview)

        benefitsList = requireView().findViewById(R.id.benefits_list)
        benefitsList.adapter = benefitsAdapter
        benefitsList.layoutManager = LinearLayoutManager(context)

        subOptionsContainer = requireView().findViewById(R.id.sub_options_container)
        subOptionsContainer.visibility = View.GONE

        periodChipGroup = requireView().findViewById(R.id.billing_period_chip_group)
        periodChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (subOffers == null) return@setOnCheckedStateChangeListener

            val offer = subOffers!!.find { it.chipId == checkedIds.first() }!!
            costOverviewTextBox.text = resources.getString(
                offer.costOverviewResId, offer.formattedPrice
            )
        }

        failedCard = requireView().findViewById(R.id.failed_card)
        failedCard.visibility = View.GONE
        failedCard.setOnClickListener { getSubOffersTrigger.onNext(0) }

        val subscribeBtn = requireView().findViewById<MaterialButton>(R.id.subscribe)
        subscribeBtn.setOnClickListener {
            Log.i(TAG, "Subscribe button clicked")
            subscribe()
        }
    }

    private fun getSubscriptionOffers(): Observable<List<SubscriptionOffer>> {
        Log.d(ReorderPagesFragment.TAG, "Get subscription offers")

        return getSubOffersTrigger
            .switchMap {
                failedCard.visibility = View.GONE
                subOptionsContainer.visibility = View.GONE

                billingSv.getSubscriptionOffers("basic")
                    .map { Optional(it) }
                    .onErrorResumeNext {
                        Log.e(TAG, "Could not fetch sub offers")
                        Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()

                        runOnUiThread {
                            failedCard.visibility = View.VISIBLE
                            subOptionsContainer.visibility = View.GONE
                        }

                        Observable.just(Optional.empty())
                    }
            }
            .filter { !it.isEmpty }
            .map { it.value }
    }

    private fun subscribe() {
        val offer = (subOffers ?: return).first { it.chipId == periodChipGroup.checkedChipId }
        Log.i(TAG, "Subscribe to offer ${offer.id}")

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(offer.productDetails!!)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(offer.offerToken!!)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
        val billingResult = billingSv.getBillingClient()
            .launchBillingFlow(requireActivity(), billingFlowParams)
    }

    companion object {
        const val TAG = "ViewSubFragment"
    }
}