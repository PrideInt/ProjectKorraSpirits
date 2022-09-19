package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.behavior.BehaviorTree.Branch;

import java.util.Arrays;

@Deprecated
public class Behaviors {
	private BehaviorTree tree;
	
	private ProtectorNature protector;
	private TerrorNature terror;
	private NightmareNature nightmare;
	
	public Behaviors() {
		this.protector = new ProtectorNature();
		this.terror = new TerrorNature();
		this.nightmare = new NightmareNature();
		
		this.tree = new BehaviorTree(protector, terror, nightmare);
	}
	public void setupTree() {
		for (int i = 0; i < 2; i++) {
			BehaviorAction[] actions = {};
			BehaviorAction[] operations = branchOf(i).behavior().behaviors().get().actions();
			
			switch (i) {
				case 0 -> {
					actions = new BehaviorAction[]{ new DefenderAct(), new HealAct() };
					protectorDefenderBranch().insert(operations[0]);
					protectorHealBranch().insert(operations[1]);
					break;
				}
				case 1 -> {
					actions = new BehaviorAction[]{ new AttackerAct(), new HealAct() };
					terrorAttackerBranch().insert(operations[0], operations[1]);
					terrorHealBranch().insert(operations[2]);
					break;
				}
				case 2 -> {
					actions = new BehaviorAction[]{ new AttackerAct() };
					nightmareAttackerBranch().insert(operations);
					break;
				}
			}
			branchOf(i).insert(actions);
		}
	}
	
	public void manageBehavior(AncientSoulweaver soulweaver) {
		switch (soulweaver.phase()) {
			case PROTECTOR -> {
				break;
			}
			case TERROR -> {
				break;
			}
			case NIGHTMARE -> {
				break;
			}
		}
	}
	
	public Branch[] branches() {
		return tree.children();
	}
	public Branch branchOf(int of) {
		return tree.children()[of];
	}
	public Branch protectorBranch() { return branchOf(0); }
	public Branch terrorBranch() { return branchOf(1); }
	public Branch nightmareBranch() { return branchOf(2); }
	
	public Branch protectorDefenderBranch() { return protectorBranch().child(0); }
	public Branch protectorHealBranch() { return protectorBranch().child(1); }
	
	public Branch terrorAttackerBranch() { return terrorBranch().child(0); }
	public Branch terrorHealBranch() { return terrorBranch().child(1); }
	
	public Branch nightmareAttackerBranch() { return nightmareBranch().child(0); }
	
	/*
			PROTECTOR BEHAVIOR
	 */
	public BehaviorAction protectorBehavior() {
		return protectorBranch().behavior();
	}
	public BehaviorAction protectorDefenderAction() {
		return protectorBranch().child(0).behavior();
	}
	public BehaviorAction protectorHealAction() {
		return protectorBranch().child(1).behavior();
	}
	
	/*
			TERROR BEHAVIOR
	 */
	public BehaviorAction terrorBehavior() {
		return terrorBranch().behavior();
	}
	public BehaviorAction terrorAttackAction() {
		return terrorBranch().child(0).behavior();
	}
	public BehaviorAction terrorHealAction() {
		return terrorBranch().child(1).behavior();
	}
	
	/*
			NIGHTMARE BEHAVIOR
	 */
	public BehaviorAction nightmareBehavior() {
		return terrorBranch().behavior();
	}
	public BehaviorAction nightmareAttackAction() {
		return terrorBranch().child(0).behavior();
	}
}
