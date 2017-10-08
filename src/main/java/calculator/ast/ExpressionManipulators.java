package calculator.ast;

import calculator.interpreter.Environment;
import calculator.errors.EvaluationError;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
import misc.exceptions.NotYetImplementedException;

/**
 * All of the static methods in this class are given the exact same parameters
 * for consistency. You can often ignore some of these parameters when
 * implementing your methods.
 *
 * Some of these methods should be recursive. You may want to consider using
 * public-private pairs in some cases.
 */
public class ExpressionManipulators {
	/**
	 * Takes the given AstNode node and attempts to convert it into a double.
	 *
	 * Returns a number AstNode containing the computed double.
	 *
	 * @throws EvaluationError
	 *             if any of the expressions contains an undefined variable.
	 * @throws EvaluationError
	 *             if any of the expressions uses an unknown operation.
	 */
	public static AstNode toDouble(Environment env, AstNode node) {
		// To help you get started, we've implemented this method for you.
		// You should fill in the TODOs in the 'toDoubleHelper' method.
		return new AstNode(toDoubleHelper(env.getVariables(), node));
	}

	private static double toDoubleHelper(IDictionary<String, AstNode> variables, AstNode node) {
		// There are three types of nodes, so we have three cases.
		if (node.isNumber()) {
			return node.getNumericValue();
		} else if (node.isVariable()) {
			if (!variables.containsKey(node.getName())) {
				// If the expression contains an undefined variable, we give up.
				throw new EvaluationError("Undefined variable: " + node.getName());
			}
			return variables.get(node.getName()).getNumericValue();
		} else {
			String name = node.getName();
			IList<AstNode> children = node.getChildren();
			if (name.equals("+")) {
				checkNumberOfOperands(children, 2);
				return toDoubleHelper(variables, children.get(0)) + toDoubleHelper(variables, children.get(1));
			} else if (name.equals("-")) {
				checkNumberOfOperands(children, 2);
				return toDoubleHelper(variables, children.get(0)) - toDoubleHelper(variables, children.get(1));
			} else if (name.equals("*")) {
				checkNumberOfOperands(children, 2);
				return toDoubleHelper(variables, children.get(0)) * toDoubleHelper(variables, children.get(1));
			} else if (name.equals("/")) {
				checkNumberOfOperands(children, 2);
				return toDoubleHelper(variables, children.get(0)) / toDoubleHelper(variables, children.get(1));
			} else if (name.equals("^")) {
				checkNumberOfOperands(children, 2);
				return Math.pow(toDoubleHelper(variables, children.get(0)), toDoubleHelper(variables, children.get(1)));
			} else if (name.equals("negate")) {
				checkNumberOfOperands(children, 1);
				return -1 * toDoubleHelper(variables, children.get(0));
			} else if (name.equals("sin")) {
				checkNumberOfOperands(children, 1);
				return Math.sin(toDoubleHelper(variables, children.get(0)));
			} else if (name.equals("cos")) {
				checkNumberOfOperands(children, 1);
				return Math.cos(toDoubleHelper(variables, children.get(0)));
			} else if (name.equals("abs")) {
				checkNumberOfOperands(children, 1);
				return Math.abs(toDoubleHelper(variables, children.get(0)));
			} else if (name.equals("exp")) {
				checkNumberOfOperands(children, 1);
				return Math.exp(toDoubleHelper(variables, children.get(0)));
			} else if (name.equals("sqrt")) {
				checkNumberOfOperands(children, 1);
				return Math.sqrt(toDoubleHelper(variables, children.get(0)));
			} else {
				throw new EvaluationError("Unknown operation: " + name);
			}
		}
	}

	// Try writing this one on your own!
	// Hint 1: Your code will likely be structured roughly similarly
	// to your "toDouble" method
	// Hint 2: When you're implementing constant folding, you may want
	// to call your "toDouble" method in some way

	// TODO: Remove this comment when we are done testing
	public static AstNode simplify(Environment env, AstNode node) {
		IDictionary<String, AstNode> vars = env.getVariables();
		if (node.isOperation() && node.getName().equals("simplify")) {
			return simplifyHelper(env, node.getChildren().get(0));
		} else {
			return simplifyHelper(env, node);
		}
	}

	public static AstNode simplifyHelper(Environment env, AstNode node) {
		// TODO: Fix simplification of sin/cos/div/abs
		IDictionary<String, AstNode> vars = env.getVariables();
		if (node.isOperation()) {
			if (isExpression(vars, node)) {
				IList<AstNode> children = node.getChildren();
				for (int i = 0; i < children.size(); i++) {
					children.set(i, simplify(env, children.get(i)));
				}
			} else {
				return new AstNode(toDoubleHelper(vars, node));
			}
		}
		return node;
	}

	/**
	 * Expected signature of plot:
	 *
	 * >>> plot(exprToPlot, var, varMin, varMax, step)
	 *
	 * Example 1:
	 *
	 * >>> plot(3 * x, x, 2, 5, 0.5)
	 *
	 * This command will plot the equation "3 * x", varying "x" from 2 to 5 in 0.5
	 * increments. In this case, this means you'll be plotting the following points:
	 *
	 * [(2, 6), (2.5, 7.5), (3, 9), (3.5, 10.5), (4, 12), (4.5, 13.5), (5, 15)]
	 *
	 * ---
	 *
	 * Another example: now, we're plotting the quadratic equation "a^2 + 4a + 4"
	 * from -10 to 10 in 0.01 increments. In this case, "a" is our "x" variable.
	 *
	 * >>> c := 4 4 >>> step := 0.01 0.01 >>> plot(a^2 + c*a + a, a, -10, 10, step)
	 *
	 * ---
	 *
	 * @throws EvaluationError
	 *             if any of the expressions contains an undefined variable.
	 * @throws EvaluationError
	 *             if varMin > varMax
	 * @throws EvaluationError
	 *             if 'var' was already defined
	 * @throws EvaluationError
	 *             if 'step' is zero or negative
	 */
	public static AstNode plot(Environment env, AstNode node) {
		String varName = node.getChildren().get(1).getName();
		for (AstNode child : node.getChildren()) {
			if (isDefined(env, child, varName)) {
				throw new EvaluationError("the expression contains an undefined variable");
			}
		}
		if (getNumericValue(env, node, 2) > getNumericValue(env, node, 3)) {
			throw new EvaluationError("varMin > varMax");
		} else if (env.getVariables().containsKey(varName)) {
			throw new EvaluationError("'var' was already defined");
		} else if (getNumericValue(env, node, 4) <= 0) {
			throw new EvaluationError("step is zero or negative");
		}
		IList<Double> xValues = null; // TODO: fix null pointer bug.
		IList<Double> yValues = null;
		for (int i = 0; i <= (getNumericValue(env, node, 3) - getNumericValue(env, node, 2))
				/ getNumericValue(env, node, 4); i++) {
			double increments = i * getNumericValue(env, node, 4);
			xValues.add(getNumericValue(env, node, 2) + increments);
			env.getVariables().put(varName, new AstNode(xValues.get(i)));
			yValues.add(toDouble(env, node.getChildren().get(1)).getNumericValue());
		}
		env.getVariables().remove(varName); // remove the value added during the loop
		env.getImageDrawer().drawScatterPlot("plot", varName, "output", xValues, yValues);
		return new AstNode(1);
	}

	private static double getNumericValue(Environment env, AstNode node, int index) {
		return toDouble(env, node.getChildren().get(index)).getNumericValue();
	}

	private static void checkNumberOfOperands(IList<AstNode> children, int count) {
		if (children.size() != count) {
			throw new EvaluationError(
					"Given " + String.valueOf(children.size()) + " operands, but expected " + String.valueOf(count));
		}
	}

	private static boolean isExpression(IDictionary<String, AstNode> variables, AstNode node) {
		if (node.isOperation()) {
			String operationName = node.getName();
			// TODO update this part for new added math functions
			if (operationName.equals("/") || operationName.equals("sin") || operationName.equals("cos")) {
				return true;
			} else {
				boolean result = false;
				for (AstNode child : node.getChildren()) {
					result |= isExpression(variables, child);
				}
				return result;
			}
		} else if (node.isVariable()) {
			if (variables.containsKey(node.getName())) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private static boolean isDefined(Environment env, AstNode node, String varName) {
		if (node.isVariable() && !node.getName().equals(varName)) {
			return env.getVariables().containsKey(node.getName());
		} else if (node.isOperation()) {
			boolean result = true;
			for (AstNode child : node.getChildren()) {
				result = isDefined(env, child, varName);
				if (!result) {
					return result;
				}
			}
		}
		return true; // when node is a number
	}
}
