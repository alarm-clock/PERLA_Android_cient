package com.example.jmb_bms.viewModel

class TeamSCTransitionManager(private val serverVM: ServerVM, private val serverInfoVM: ServerInfoVM) {

    fun changingToServerVM()
    {
        serverVM.bindService()
        //serverInfoVM.resetState()
    }
}