/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visitor.sets.test;

import com.visitor.card.types.Unit;
import com.visitor.game.Game;
import com.visitor.helpers.CounterMap;

import static com.visitor.card.properties.Combat.CombatAbility.FirstStrike;
import static com.visitor.card.properties.Combat.CombatAbility.Vigilance;

/**
 * @author pseudo
 */
public class FirstStriky extends Unit {

    public FirstStriky(Game game, String owner) {
        super(game, "First Striky",
                0, new CounterMap(),
                "",
                3, 1,
                owner, FirstStrike);
    }
}
