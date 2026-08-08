package com.halashasneen.nova.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.databinding.FragmentHomeBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.LoadAdError

class HomeFragment : Fragment() {

    private var rewardedAd: RewardedAd? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnScan.setOnClickListener {
            findNavController().navigate(com.halashasneen.nova.R.id.action_home_to_scan)
        }
        binding.btnCreate.setOnClickListener {
            findNavController().navigate(com.halashasneen.nova.R.id.action_home_to_create)
        }
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(com.halashasneen.nova.R.id.action_home_to_history)
        }
        binding.btnVault.setOnClickListener {
            findNavController().navigate(com.halashasneen.nova.R.id.action_home_to_vault)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(com.halashasneen.nova.R.id.action_home_to_settings)
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvScansCount.text = stats.totalScans.toString()
            binding.tvGeneratedCount.text = stats.totalGenerated.toString()
            binding.tvVaultCount.text = stats.vaultItemsCount.toString()
        }

        val settingsRepo = (requireActivity().application as QRProApplication).settingsRepository

        if (!settingsRepo.isAdFreeActive()) {
            val adRequest = AdRequest.Builder().build()
            binding.adViewHome.loadAd(adRequest)
        } else {
            binding.adViewHome.visibility = View.GONE
        }

        loadRewardedAd()

        binding.btnWatchAd.setOnClickListener {
            rewardedAd?.show(requireActivity()) {
                settingsRepo.grantAdFreeForOneHour()
                binding.adViewHome.visibility = View.GONE
            } ?: run {
                loadRewardedAd()
            }
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            requireContext(),
            "ca-app-pub-5961173995415325/7054312718",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats() // تحديث الإحصائيات كل مرة يرجع فيها المستخدم للرئيسية
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
