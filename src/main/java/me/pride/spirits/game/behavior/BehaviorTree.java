package me.pride.spirits.game.behavior;

import java.util.ArrayList;
import java.util.List;

public class BehaviorTree {
	private Branch[] root;
	
	public BehaviorTree(BehaviorAction... behaviors) {
		init(behaviors);
	}
	protected <T extends BehaviorAction> BehaviorTree init(T... behaviors) {
		this.root = new Branch[behaviors.length];
		for (int i = 0; i < behaviors.length; i++) {
			this.root[i] = new Branch<T>(behaviors[i]);
		}
		return this;
	}
	protected <T extends BehaviorAction> void insert(int childIndex, int depth, int breadth, T... behaviors) {
		if (root != null) {
			Branch child = root[childIndex];
			
			if (depth == 0) {
				for (T behavior : behaviors) {
					child.insert(new Branch<T>(behavior));
				}
				return;
			}
			for (int i = 0; i < depth; i++) {
				child = child.child(breadth);
			}
			for (T behavior : behaviors) {
				child.insert(new Branch<T>(behavior));
			}
		}
	}
	protected Branch[] children() {
		return this.root;
	}
	protected void imported(Branch branch) {
	
	}
	class Branch<T extends BehaviorAction> {
		private List<Branch<T>> children;
		private T behavior;
		
		protected Branch(T behavior) {
			this.children = new ArrayList<>();
			this.behavior = behavior;
		}
		protected T behavior() {
			return this.behavior;
		}
		protected List<Branch<T>> children() {
			return this.children;
		}
		protected Branch<T> child(int child) {
			return children.get(child);
		}
		protected void insert(Branch<T>... branches) {
			for (Branch<T> branch : branches) {
				children.add(branch);
			}
		}
		protected void insert(T... actions) {
			for (T action : actions) {
				children.add(new Branch<T>(action));
			}
		}
		protected boolean hasChild() {
			return !children.isEmpty();
		}
	}
}
