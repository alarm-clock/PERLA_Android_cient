package com.example.jmb_bms.viewModel.server

import com.example.jmb_bms.viewModel.server.ServerInfoVM
import com.example.jmb_bms.viewModel.server.ServerVM

class TeamSCTransitionManager(private val serverVM: ServerVM, private val serverInfoVM: ServerInfoVM) {

    fun changingToServerVM(ip: String, port: Int)
    {
        serverVM.sethost(ip,port)
        serverVM.bindService()
        //serverInfoVM.resetState()
    }
}