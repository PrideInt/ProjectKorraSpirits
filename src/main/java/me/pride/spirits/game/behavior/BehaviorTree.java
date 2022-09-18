package me.pride.spirits.game.behavior;

import java.util.ArrayList;
import java.util.List;

public class BehaviorTree {
	private BehaviorTree root;
	private Branch[] children;
	
	public BehaviorTree() {
		this.root = new BehaviorTree();
	}
	public BehaviorTree(Behavior... behaviors) {
		this();
		init(behaviors);
	}
	public BehaviorTree init(Behavior... behaviors) {
		this.children = new Branch[behaviors.length];
		for (int i = 0; i < behaviors.length; i++) {
			this.children[i] = new Branch(behaviors[i]);
		}
		return this;
	}
	public void insert(int childIndex, int depth, int breadth, Behavior... behaviors) {
		if (children != null) {
			Branch child = children[childIndex];
			
			if (depth == 0) {
				for (Behavior behavior : behaviors) {
					child.insert(new Branch(behavior));
				}
				return;
			}
			for (int i = 0; i < depth; i++) {
				child = child.child(breadth);
			}
			for (Behavior behavior : behaviors) {
				child.insert(new Branch(behavior));
			}
		}
	}
	class Branch {
		private List<Branch> children;
		private Behavior behavior;
		
		public Branch(Behavior behavior) {
			this.children = new ArrayList<>();
			this.behavior = behavior;
		}
		public Behavior behavior() {
			return this.behavior;
		}
		public List<Branch> children() {
			return this.children;
		}
		public Branch child(int child) {
			return children.get(child);
		}
		public void insert(Branch... branches) {
			for (Branch branch : branches) {
				children.add(branch);
			}
		}
		public boolean hasChild() {
			return children.isEmpty();
		}
	}
}
