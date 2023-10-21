package com.example.jmb_bms.data

import com.example.jmb_bms.R
import com.example.jmb_bms.model.Affirmation

class ListData {

    fun loadAffirmaions(): List<Affirmation>
    {
        return listOf<Affirmation>(
            Affirmation(R.string.Main_menu_Point_mng , R.mipmap.points_icon , 1),
            Affirmation(R.string.Main_menu_settings , R.mipmap.settings_icon , 2),
            Affirmation(R.string.Main_menu_chat , R.mipmap.chat_icon, 3),
            Affirmation(R.string.Main_menu_orders , R.mipmap.quick_chat_icon , 4),
            Affirmation(R.string.Main_menu_team , R.mipmap.team_icon , 5)

        )
    }
}