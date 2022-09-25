package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.behavior.BehaviorTree.Branch;

import me.pride.spirits.game.behavior.ProtectorNature.*;
import me.pride.spirits.game.behavior.TerrorNature.*;
import me.pride.spirits.game.behavior.NightmareNature.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Behaviors {
	private BehaviorTree tree;
	private Branch branch;
	
	private ProtectorNature protector;
	private TerrorNature terror;
	private NightmareNature nightmare;
	
	private final int ATTACKER_ACTION = 0, DEFENDER_ACTION = 1, HEAL_ACTION = 2;
	
	private Forcefield forcefield;
	private RandomHeal randomHeal;
	
	private FangsCircle fangsCircle;
	private FangsLine fangsLine;
	private HealingStasis healingStasis;
	
	private SummonWraith summonWraith;
	private SummonObelisk summonObelisk;
	private CauseInsanity insanity;
	
	private NightmareCycle nightmareCycle;
	
	public Behaviors() {
		this.protector = new ProtectorNature();
		this.terror = new TerrorNature();
		this.nightmare = new NightmareNature();
		
		this.nightmareCycle = new NightmareCycle();
		
		this.tree = new BehaviorTree(protector, terror, nightmare);
		this.branch = tree.children()[0];
	}
	public void setupTree() {
		for (Branch<BehaviorAction> branch : tree.children()) {
			branch.insert(new AttackerAct(), new DefenderAct(), new HealAct());
		}
		for (int i = 0; i < tree.children().length; i++) {
			Optional<BehaviorRecord> behaviorRecord = tree.children()[i].behavior().behavioralRecord();
			
			if (behaviorRecord.isPresent()) {
				BehaviorRecord record = behaviorRecord.get();
				
				for (BehaviorAction action : record.actions()) {
					if (action instanceof AttackerAct) {
						tree.children()[i].child(ATTACKER_ACTION).insert(action);
						
					} else if (action instanceof DefenderAct) {
						tree.children()[i].child(DEFENDER_ACTION).insert(action);
						
					} else if (action instanceof HealAct) {
						tree.children()[i].child(HEAL_ACTION).insert(action);
					}
				}
			}
		}
		/*
		for (int i = 0; i < tree.children().length; i++) {
			System.out.println(tree.children()[i].behavior().toString());
			
			for (int j = 0; j < tree.children()[i].children().size(); j++) {
				Branch branch = tree.children()[i].child(j);
				BehaviorAction action = tree.children()[i].child(j).behavior();
				
				if (action instanceof Action) {
					System.out.println("   " + ((Action) action).name());
				}
				for (int k = 0; k < branch.children().size(); k++) {
					BehaviorAction operation = branch.child(k).behavior();
					
					if (operation instanceof Action) {
						System.out.println("      " + ((Action) operation).name());
					}
				}
			}
		}
		 */
		this.forcefield = (Forcefield) forcefield();
		this.randomHeal = (RandomHeal) randomHeal();
		
		this.fangsCircle = (FangsCircle) fangsCircle();
		this.fangsLine = (FangsLine) fangsLine();
		this.healingStasis = (HealingStasis) healingStasis();
		
		this.summonWraith = (SummonWraith) wraith();
		this.summonObelisk = (SummonObelisk) obelisk();
		this.insanity = (CauseInsanity) insanity();
	}
	public void manageBehavior(AncientSoulweaver soulweaver) {
		if (!soulweaver.healthAtNightmare()) {
			nightmareCycle.nightmareCycle(soulweaver);
		}
		switch (soulweaver.phase()) {
			case PROTECTOR -> {
				if (!forcefield.progressing() && !forcefield.inCooldown()) {
					forcefield.attune(0, ThreadLocalRandom.current().nextInt(4, 8), soulweaver.entity().getLocation());
				
				} else if (!forcefield.progressing() && !randomHeal.inCooldown()) {
					randomHeal.doAction(soulweaver);
				}
				if (!forcefield.inCooldown()) {
					forcefield.doAction(soulweaver);
				}
				break;
			}
			case TERROR -> {
				boolean stasis = false;
				if (!healingStasis.progressing()) {
					if (!fangsCircle.progressing() && !fangsCircle.inCooldown() || !fangsLine.progressing() && !fangsLine.inCooldown()) {
						stasis = ThreadLocalRandom.current().nextInt(0, 10) == 4;
					}
					if (fangsCircle.inCooldown()) {
						if (!fangsLine.progressing()) {
							fangsLine.attune(soulweaver);
						}
						fangsLine.doAction(soulweaver);
						
					} else if (fangsLine.inCooldown()) {
						fangsCircle.doAction(soulweaver);
					}
				}
				if (fangsCircle.inCooldown() && fangsLine.inCooldown() && stasis) {
					healingStasis.doAction(soulweaver);
					
					if (healingStasis.inCooldown()) {
						stasis = false;
					}
				}
				break;
			}
			case NIGHTMARE -> {
				break;
			}
		}
	}
	
	public Branch<BehaviorAction>[] branches() {
		return tree.children();
	}
	public Branch<Action> branchOf(int of) {
		return tree.children()[of];
	}
	private Branch<Action> protectorBranch() { return branchOf(0); }
	private List<Branch<Action>> protectorChildren() {
		return protectorBranch().children();
	}
	
	private Branch<Action> terrorBranch() { return branchOf(1); }
	private List<Branch<Action>> terrorChildren() {
		return terrorBranch().children();
	}
	
	private Branch<Action> nightmareBranch() { return branchOf(2); }
	private List<Branch<Action>> nightmareChildren() {
		return nightmareBranch().children();
	}
	
	private Action forcefield() { return protectorChildren().get(DEFENDER_ACTION).child(0).behavior(); }
	private Action randomHeal() { return protectorChildren().get(HEAL_ACTION).child(0).behavior(); }
	
	private Action fangsCircle() { return terrorChildren().get(ATTACKER_ACTION).child(0).behavior(); }
	private Action fangsLine() { return terrorChildren().get(ATTACKER_ACTION).child(1).behavior(); }
	private Action healingStasis() { return terrorChildren().get(HEAL_ACTION).child(0).behavior(); }
	
	private Action wraith() { return nightmareChildren().get(ATTACKER_ACTION).child(0).behavior(); }
	private Action obelisk() { return nightmareChildren().get(ATTACKER_ACTION).child(1).behavior(); }
	private Action insanity() { return nightmareChildren().get(ATTACKER_ACTION).child(2).behavior(); }
}
