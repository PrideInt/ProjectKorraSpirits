package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.behavior.BehaviorTree.Branch;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Deprecated
public class Behaviors {
	private BehaviorTree tree;
	private Branch branch;
	
	private ProtectorNature protector;
	private TerrorNature terror;
	private NightmareNature nightmare;
	
	private final int ATTACKER_ACTION = 0, DEFENDER_ACTION = 1, HEAL_ACTION = 2;
	
	public Behaviors() {
		this.protector = new ProtectorNature();
		this.terror = new TerrorNature();
		this.nightmare = new NightmareNature();
		
		this.tree = new BehaviorTree(protector, terror, nightmare);
		this.branch = tree.children()[0];
	}
	public void setupTree() {
		for (Branch<BehaviorAction> branch : branches()) {
			branch.insert(new Action[]{ new AttackerAct(), new DefenderAct(), new HealAct() });
		}
		for (int i = 0; i < 2; i++) {
			Action[] operations = (Action[]) branchOf(i).behavior().behavioralRecord().get().actions();
			
			switch (i) {
				case ATTACKER_ACTION -> {
					terrorChildren().get(i).insert(operations[0], operations[1]); // Fangs
					nightmareChildren().get(i).insert(operations); // All attacking operations
					break;
				}
				case DEFENDER_ACTION -> {
					protectorChildren().get(i).insert(operations[0]); // Forcefield
					break;
				}
				case HEAL_ACTION -> {
					protectorChildren().get(i).insert(operations[1]); // Random heal
					terrorChildren().get(i).insert(operations[2]); // Healing stasis
					break;
				}
			}
		}
	}
	public void manageBehavior(AncientSoulweaver soulweaver) {
		switch (soulweaver.phase()) {
			case PROTECTOR -> {
				for (Branch<Action> branch : protectorChildren()) {
					branch.children().forEach(typeBranch -> {
						List<Branch<Action>> children = typeBranch.children();
						
						switch (typeBranch.behavior().name()) {
							case "Defending" -> {
								ProtectorNature.Forcefield forcefield = (ProtectorNature.Forcefield) forcefield();
								if (!forcefield.progressing()) {
									forcefield.setLocation(soulweaver.entity().getLocation());
								}
								break;
							}
							case "Healing" -> {
							
							}
						}
						children.forEach(child -> {
							Action action = child.behavior();
							
							if (!action.inCooldown()) action.doAction(soulweaver);
						});
					});
				}
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
	
	public Branch<BehaviorAction>[] branches() {
		return tree.children();
	}
	public Branch<Action> branchOf(int of) {
		return tree.children()[of];
	}
	public Branch<Action> protectorBranch() { return branchOf(0); }
	public List<Branch<Action>> protectorChildren() {
		return protectorBranch().children();
	}
	
	public Branch<Action> terrorBranch() { return branchOf(1); }
	public List<Branch<Action>> terrorChildren() {
		return terrorBranch().children();
	}
	
	public Branch<Action> nightmareBranch() { return branchOf(2); }
	public List<Branch<Action>> nightmareChildren() {
		return nightmareBranch().children();
	}
	
	private Action forcefield() { return protectorChildren().get(DEFENDER_ACTION).child(0).behavior(); }
	private Action randomHeal() { return protectorChildren().get(HEAL_ACTION).child(0).behavior(); }
	
	private Action fangsCircle() { return terrorChildren().get(ATTACKER_ACTION).child(0).behavior(); }
	private Action fangsLine() { return terrorChildren().get(ATTACKER_ACTION).child(1).behavior(); }
	private Action healingStasis() { return terrorChildren().get(HEAL_ACTION).child(0).behavior(); }
	
	private Action wraith() { return nightmareChildren().get(ATTACKER_ACTION).child(0).behavior(); }
	private Action obelisk() { return nightmareChildren().get(ATTACKER_ACTION).child(2).behavior(); }
	private Action insanity() { return nightmareChildren().get(ATTACKER_ACTION).child(3).behavior(); }
}
