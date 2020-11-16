package com.visitor.sets.base;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.visitor.card.types.Tome;
import com.visitor.game.Game;
import com.visitor.helpers.CounterMap;
import com.visitor.helpers.Hashmap;
import com.visitor.protocol.Types;

import static com.visitor.protocol.Types.Knowledge.GREEN;

/**
 * @author pseudo
 */
public class PersonalRelations extends Tome {

    public PersonalRelations(Game game, String owner) {
        super(game, "Personal Relations", "Study: Gain {G}{G}", owner, new CounterMap<>(GREEN, 2));
    }

}