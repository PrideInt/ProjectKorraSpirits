package me.pride.spirits.util.objects;

public class QuadraticInterpolator implements Interpolator {
	private boolean mode;
	
	@Override
	public double[] interpolate(double from, double to, int max) {
		final double[] results = new double[max];
		if (mode) {
			double a = (to - from) / (max * max);
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + from;
			}
		} else {
			double a = (from - to) / (max * max);
			double b = - 2 * a * max;
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + b * i + from;
			}
		}
		return results;
	}
	public QuadraticInterpolator mode(boolean mode) {
		this.mode = mode;
		return this;
	}
}