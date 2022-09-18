package me.pride.spirits.game.behavior;

public class Behavior {
	private BehaviorTree tree;
	
	public Behavior() {
		this.tree = new BehaviorTree(new ProtectorNature(), new TerrorNature(), new NightmareNature());
		
		this.tree.insert(0, 0, 0, new DefenderAct(), new HealAct());
		this.tree.insert(1, 0, 0, new AttackerAct(), new HealAct());
		this.tree.insert(2, 0, 0, new AttackerAct());
	}
	
	public static void formTree() {
	
	}
}

class DefenderAct extends Behavior {

}
class AttackerAct extends Behavior {

}
class HealAct extends Behavior {

}
