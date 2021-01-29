package com.visitor.game;

import com.visitor.card.properties.*;
import com.visitor.card.types.helpers.AbilityCard;
import com.visitor.helpers.Arraylist;
import com.visitor.helpers.CounterMap;
import com.visitor.helpers.HelperFunctions;
import com.visitor.helpers.containers.ActivatedAbility;
import com.visitor.helpers.containers.Damage;
import com.visitor.protocol.Types;
import com.visitor.protocol.Types.Counter;
import com.visitor.protocol.Types.CounterGroup;
import com.visitor.protocol.Types.Knowledge;
import com.visitor.protocol.Types.KnowledgeGroup;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.visitor.game.Game.Zone.*;
import static java.util.UUID.randomUUID;

/**
 * @author pseudo
 */
public abstract class Card implements Serializable {

	protected final Game game;
	public UUID id;
	public String name;
	public String text;
	public Arraylist<CardType> types;
	public Arraylist<CardSubtype> subtypes;
	public CounterMap<Knowledge> knowledge;

	public UUID owner;
	public UUID controller;
	public Game.Zone zone;
	protected boolean depleted;
	protected CounterMap<Counter> counters;
	public Arraylist<UUID> targets;
	public Arraylist<UUID> attachments;

	protected Activatable activatable;
	protected Triggering triggering;
	protected Combat combat;
	protected Playable playable;
	protected Studiable studiable;
	protected Attachable attachable;

	protected Runnable enterPlayEffect = ()->{};
	protected Runnable leavePlayEffect = ()->{};


	/**
	 * This is the default constructor for creating a card.
	 *
	 * @param name
	 * @param knowledge
	 * @param text
	 * @param owner
	 */
	public Card (Game game, String name,
	             CounterMap<Knowledge> knowledge,
	             CardType type, String text, UUID owner) {
		this.game = game;
		id = randomUUID();
		counters = new CounterMap<>();
		types = new Arraylist<>(type);
		subtypes = new Arraylist<>();
		targets = new Arraylist<>();
		attachments = new Arraylist<>();

		this.name = name;
		this.knowledge = knowledge;
		this.text = text;
		this.owner = owner;
		this.controller = owner;
		this.depleted = false;
	}


	/**
	 * Function that adds counters to the card.
	 *
	 * @param name
	 * @param count
	 */
	public void addCounters (Counter name, int count) {
		counters.add(name, count);
	}

	public void removeCounters (Counter name, int count) {
		counters.decrease(name, count);
	}

	public boolean hasCounters (Counter name, int i) {
		return counters.getOrDefault(name, 0) >= i;
	}

	public void deplete () {
		depleted = true;
	}

	public void newTurn () {
		depleted = false;
		if (combat != null)
			combat.newTurn();
	}

	/**
	 * Function that clears status flags and supplementary data of the card.
	 */
	public void clear () {
		depleted = false;
		targets.clear();
		attachments.clear();
		if (combat != null)
			combat.clear();
		if (attachable != null)
			attachable.clear();
	}

	public void enterPlay(){
		if (triggering != null) {
			triggering.register();
		}
		if (attachable != null) {
			attachable.attach();
		}
		enterPlayEffect.run();
		game.addEvent(Event.enterPlay(this), true);
	}

	public void leavePlay () {
		if (triggering != null) {
			triggering.deregister();
		}
		if (attachable != null) {
			attachable.removeFromAttached();
		}
		attachments.forEach(game::destroy);
		clear();
		leavePlayEffect.run();
	}

	public void moveToZone(Game.Zone zone){
		if (this.zone == Play && (zone != Play && zone != Opponent_Play)) {
			leavePlay();
		}
		game.extractCard(id);
		this.zone = zone;
		game.putTo(controller, this, zone);
	}

	public void destroy () {
		moveToZone(Discard_Pile);
	}

	public void sacrifice () {
		moveToZone(Discard_Pile);
	}

	public void returnToHand () {
		moveToZone(Hand);
	}

	public void purge () {
		moveToZone(Game.Zone.Void);
	}

	/*
	public void copyPropertiesFrom (Card c) {

		id = c.id;
		owner = c.owner;
		controller = c.controller;
		counters = c.counters;
		depleted = c.depleted;
		targets = c.targets;
	}
	*/

	private void runIfNotNull (Object object, Runnable runnable) {
		HelperFunctions.runIfNotNull(object, runnable, () ->
				System.out.println(toCardMessage().toString())
		);
	}

	private <T> T runIfNotNull (Object object, Supplier<T> supplier) {
		return HelperFunctions.runIfNotNull(object, supplier, () -> {
			System.out.println(toCardMessage().toString());
			return null;
		});
	}

	public final void play (boolean withCost) {
		runIfNotNull(playable, () -> playable.play(withCost));
	}

	public final void study (Player player, boolean normal) {
		runIfNotNull(studiable, () -> studiable.study(player, normal));
	}

	public final void checkEvent (Event e) {
		runIfNotNull(triggering, () -> triggering.checkEvent(e));
	}

	public final void resolve () {
		runIfNotNull(playable, () -> playable.resolve());
	}

	public final void receiveDamage (Damage damage, Card source) {
		runIfNotNull(combat, () -> combat.receiveDamage(damage, source));
	}

	public void heal (int healAmount) {
		runIfNotNull(combat, () -> combat.heal(healAmount));
	}

	public final int getAttack () {
		return runIfNotNull(combat, () -> combat.getAttack());
	}

	public void gainHealth (int health) {
		runIfNotNull(combat, () -> combat.addHealth(health));
	}

	//TODO: Refactor these like above
	public final void unsetAttacking () {
		runIfNotNull(combat, combat::unsetAttacking);
	}

	public final void addBlocker (UUID blockerId) {
		runIfNotNull(combat, ()-> combat.addBlocker(blockerId));
	}

	public final void setBlocking (UUID blockedBy) {
		runIfNotNull(combat, ()-> combat.setBlocking(blockedBy));
	}

	public final void dealAttackDamage (boolean firstStrike) {
			runIfNotNull(combat, ()->
					combat.dealAttackDamage(firstStrike));
	}

	public final void dealBlockDamage () {
		runIfNotNull(combat, ()->
				combat.dealBlockDamage());
	}

	public final void activate () {
		if (activatable != null) {
			activatable.activate();
		} else {
			System.out.println("Trying to activate a non-activatable card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public final void unsetBlocking () {
		if (combat != null) {
			combat.unsetBlocking();
		} else {
			System.out.println("Trying to unset blocking of a non-combat card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public boolean canDieFromBlock () {
		if (combat != null) {
			return combat.canDieFromBlock();
		} else {
			System.out.println("Trying to check death of a non-combat card!");
			System.out.println(toCardMessage().toString());
			return false;
		}
	}

	public boolean canDieFromAttack () {
		if (combat != null) {
			return combat.canDieFromAttack();
		} else {
			System.out.println("Trying to check death of a non-combat card!");
			System.out.println(toCardMessage().toString());
			return false;
		}
	}

	public void maybeDieFromBlock () {
		if (canDieFromBlock()) {
			game.destroy(id);
		} else {
			System.out.println("Trying to kill a non-combat card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public void maybeDieFromAttack () {
		if (canDieFromAttack()) {
			game.destroy(id);
		} else {
			System.out.println("Trying to kill a non-combat card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public void addCombatAbility (Combat.CombatAbility ability) {
		if (combat != null) {
			combat.addCombatAbility(ability);
		} else {
			System.out.println("Trying to add combat ability to a non-combat card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public void removeCombatAbility (Combat.CombatAbility ability) {
		if (combat != null) {
			combat.removeCombatAbility(ability);
		} else {
			System.out.println("Trying to remove combat ability to a non-combat card!");
			System.out.println(toCardMessage().toString());
		}
	}

	public final boolean canAttack () {
		return combat != null && combat.canAttack();
	}

	public final boolean canBlock () {
		return combat != null && combat.canBlock();
	}

	public final boolean canBlock (Card c) {
		return combat != null && combat.canBlock(c);
	}

	public final boolean canPlay (boolean withCost) {
		return playable != null && playable.canPlay(withCost);
	}

	public final boolean canActivate () {
		return activatable != null && activatable.canActivate();
	}

	public final boolean canStudy () {
		return studiable != null && studiable.canStudy();
	}

	public final boolean isAttacking () {
		return combat != null && combat.isAttacking();
	}

	public final void setAttacking (UUID attacker) {
		runIfNotNull(combat, () -> combat.setAttacking(attacker));
	}

	public final boolean hasCombatAbility (Combat.CombatAbility combatAbility) {
		return combat != null && combat.hasCombatAbility(combatAbility);
	}

	public boolean hasType (CardType type) {
		return types.contains(type);
	}

	public boolean hasSubtype (CardSubtype type) {
		return subtypes.contains(type);
	}

	public Types.Card.Builder toCardMessage () {
		String packageName = this.getClass().getPackageName();
		String set = packageName.substring(packageName.lastIndexOf(".") + 1);
		Types.Card.Builder builder = Types.Card.newBuilder()
				.setId(id.toString())
				.setSet(set)
				.setName(name)
				.setDepleted(depleted)
				.setDescription(text)
				.setLoyalty(-1)
				.addAllTypes(types.transformToStringList())
				.addAllSubtypes(subtypes.transformToStringList())
				.addAllTargets(targets.transformToStringList())
				.addAllAttachments(attachments.transformToStringList());

		counters.forEach((k, i) -> builder.addCounters(CounterGroup.newBuilder()
				.setCounter(k)
				.setCount(i).build()));

		knowledge.forEach((k, i) -> builder.addKnowledgeCost(KnowledgeGroup.newBuilder()
				.setKnowledge(k)
				.setCount(i).build()));

		if (playable != null)
			builder.setCost(playable.getCost() + "");

		if (combat != null) {
			builder.setCombat(combat.toCombatMessage());
		}

		return builder;
	}



	public void endTurn () {
		runIfNotNull(combat, () -> combat.endTurn());
		runIfNotNull(activatable, () -> activatable.endTurn());
	}

	public void addTurnlyCombatAbility (Combat.CombatAbility combatAbility) {
		runIfNotNull(combat, () -> combat.addTurnlyCombatAbility(combatAbility));
	}

	public void addTurnlyAttack (int attack) {
		runIfNotNull(combat, () -> combat.addTurnlyAttack(attack));
	}

	public void addTurnlyHealth (int health) {
		runIfNotNull(combat, () -> combat.addTurnlyHealth(health));
	}

	public void addTurnlyActivatedAbility (ActivatedAbility ability) {
		runIfNotNull(activatable, ()-> activatable.addTurnlyActivatedAbility(ability));
	}



	public void addAttack (int i) {
		runIfNotNull(combat, () -> combat.addAttack(i));
	}

	public void addHealth (int i) {
		runIfNotNull(combat, () -> combat.addHealth(i));
	}

	public void addShield (int i) {
		runIfNotNull(combat, ()->combat.addShield(i));
	}


	public void setAttack (int i) {
		runIfNotNull(combat, () -> combat.setAttack(i));
	}

	public void setHealth (int i) {
		runIfNotNull(combat, () -> combat.setHealth(i));
	}


	public boolean isDamagable () {
		return combat != null;
	}

	public boolean isStudiable () {
		return studiable != null;
	}

	public boolean hasColor (Knowledge knowledge) {
		return this.knowledge.contains(knowledge);
	}

	public boolean isDeploying () {
		return combat != null && combat.isDeploying();
	}

	public boolean isDepleted () {
		return depleted;
	}


	public int getHealth () {
		return runIfNotNull(combat, ()-> combat.getHealth());
	}

	public boolean isColorless () {
		return knowledge.isEmpty();
	}

	/** Enter Play Effect setters
	 *
	 */
	public void addEnterPlayEffectOnStack (CounterMap<Types.Knowledge> knowledge, String text, Runnable effect){
		if (knowledge == null){
			knowledge = new CounterMap<>();
		}
		CounterMap<Types.Knowledge> finalKnowledge = knowledge;
		enterPlayEffect = () -> game.runIfHasKnowledge(controller, finalKnowledge, ()->game.addToStack(new AbilityCard(game, this, text, effect)));
	}

	public void addEnterPlayEffect (CounterMap<Types.Knowledge> knowledge, Runnable effect){
		if (knowledge == null){
			knowledge = new CounterMap<>();
		}
		CounterMap<Types.Knowledge> finalKnowledge = knowledge;
		enterPlayEffect = () -> game.runIfHasKnowledge(controller, finalKnowledge, effect);
	}

	public void addLeavePlayEffect (CounterMap<Types.Knowledge> knowledge, Runnable effect){
		if (knowledge == null){
			knowledge = new CounterMap<>();
		}
		CounterMap<Types.Knowledge> finalKnowledge = knowledge;
		leavePlayEffect = () -> game.runIfHasKnowledge(controller, finalKnowledge, effect);
	}

	public boolean isPlayable () { return playable != null;	}

	public int getCost () { return playable.getCost();	}

	public void setPurging () {
		runIfNotNull(playable, ()-> playable.setPurging());
	}

	public void setDonating()
	{
		addEnterPlayEffect(null, ()-> game.donate(id, game.getOpponentId(controller), Game.Zone.Play));
	}

	public void addAttachment (UUID attachmentId) {
		attachments.add(attachmentId);
	}

	public void removeAttachment (UUID attachmentId) {
		attachments.remove(attachmentId);
	}

	public void setAttachable (Predicate<Card> validTarget, Consumer<UUID> afterAttachEffect, Consumer<UUID> afterRemoveEffect) {
		attachable = new Attachable(game, this, validTarget, afterAttachEffect, afterRemoveEffect);
		playable.setTargetSingleCard(Game.Zone.Both_Play, validTarget, "Choose a card to attach", attachable::setAttachTo, null);
	}

	public void loseAttack (int attack) {
		runIfNotNull(combat, () -> combat.loseAttack(attack));
	}

	public void loseHealth (int health) {
		runIfNotNull(combat, () -> {
			combat.loseHealth(health);
			if (combat.getHealth() == 0)
				game.destroy(id);
		});
	}

	public enum CardType {
		Ally,
		Asset,
		Junk,
		Passive,
		Spell,
		Tome,
		Unit,
		Attachment,

		// Internal types
		Ability,
		Effect
	}

	public enum CardSubtype {
		// Spell subtypes
		Cantrip,
		Ritual,

		// Unit Subtypes
		Zombie,
		Wizard,
		Bat,
		Spirit,
		Warrior,
		Golem,
		Elf,
		Insect, Plant, Wurm, Wolf
	}

}