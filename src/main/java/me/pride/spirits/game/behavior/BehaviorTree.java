package me.pride.spirits.game.behavior;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class BehaviorTree {
	private Branch[] root;
	
	public BehaviorTree(BehaviorAction... behaviors) {
		init(behaviors);
	}
	protected BehaviorTree init(BehaviorAction... behaviors) {
		this.root = new Branch[behaviors.length];
		for (int i = 0; i < behaviors.length; i++) {
			this.root[i] = new Branch(behaviors[i]);
		}
		return this;
	}
	protected void insert(int childIndex, int depth, int breadth, BehaviorAction... behaviors) {
		if (root != null) {
			Branch child = root[childIndex];
			
			if (depth == 0) {
				for (BehaviorAction behavior : behaviors) {
					child.insert(new Branch(behavior));
				}
				return;
			}
			for (int i = 0; i < depth; i++) {
				child = child.child(breadth);
			}
			for (BehaviorAction behavior : behaviors) {
				child.insert(new Branch(behavior));
			}
		}
	}
	protected Branch[] children() {
		return this.root;
	}
	protected void imported(Branch branch) {
	
	}
	class Branch {
		private List<Branch> children;
		private BehaviorAction behavior;
		
		protected Branch(BehaviorAction behavior) {
			this.children = new ArrayList<>();
			this.behavior = behavior;
		}
		protected BehaviorAction behavior() {
			return this.behavior;
		}
		protected List<Branch> children() {
			return this.children;
		}
		protected Branch child(int child) {
			return children.get(child);
		}
		protected void insert(Branch... branches) {
			for (Branch branch : branches) {
				children.add(branch);
			}
		}
		protected void insert(BehaviorAction... actions) {
			for (BehaviorAction action : actions) {
				children.add(new Branch(action));
			}
		}
		protected boolean hasChild() {
			return children.isEmpty();
		}
	}
}
