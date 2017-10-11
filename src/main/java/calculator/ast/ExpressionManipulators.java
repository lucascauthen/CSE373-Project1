package calculator.ast;

import calculator.interpreter.Environment;
import calculator.errors.EvaluationError;
import datastructures.concrete.DoubleLinkedList;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;

//This is a part of a Symbolic algebra calculator. It does not immediately
//evaluate the expressions the user type in buy instead lets user manipulate
//them symbolically. It can simplify an expression, convert an expression to
//a double and plot an expression. 
public class ExpressionManipulators {
	// Pass an Environment object and AstNode object as parameters.
	// return a node which attempts to evaluate the given
	// AstNode into a single double node. Return an AstNode containing
	// the computed double
	// Throws EvaluationError if any of the expressions contains an
	// undefined variable
	// Throw EvaluationError if any of the expressions uses an unknown operation
	public static AstNode toDouble(Environment env, AstNode node) {
		return new AstNode(toDoubleHelper(env.getVariables(), node));
	}

	// Pass an IDictionary<> Object and a AstNode object as parameters.
	// return a double which attempts to evaluate the given AstNode.
	// Throws EvaluationError if any of the expressions contains an
	// undefined variable
	// Throw EvaluationError if any of the expressions uses an unknown operation
	private static double toDoubleHelper(IDictionary<String, AstNode> variables, AstNode node) {
		if (node.isNumber()) {
			return node.getNumericValue();
		} else if (node.isVariable()) {
			if (!variables.containsKey(node.getName())) {
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
			} else if (name.equals("toDouble")) {
				checkNumberOfOperands(children, 1);
				return toDoubleHelper(variables, simplifyHelper(variables, node.getChildren().get(0)));
			} else {
				throw new EvaluationError("Unknown operation: " + name);
			}
		}
	}

	// Takes in the environment object as well as a root node of an expression tree
	// Handles the simplify(...) operation, and returns a simplified version of the
	// current node
    // Throw EvaluationError if the simplify(...) operation contains the incorrect
	// number of operands
	public static AstNode simplify(Environment env, AstNode node) {
		IDictionary<String, AstNode> vars = env.getVariables();
		if (node.isOperation()) {
			String operation = node.getName();
			if (operation.equals("simplify")) {
				checkNumberOfOperands(node.getChildren(), 1);
				return simplifyHelper(vars, node.getChildren().get(0));
			} else {
				return simplifyHelper(vars, node);
			}
		} else {
			return simplifyHelper(vars, node);
		}
	}

	// Takes in the current valid variables and a node relative to simplify
	// Returns a simplified version of the current node and that's node children
	// Does not modify the current node or that node's children
	public static AstNode simplifyHelper(IDictionary<String, AstNode> vars, AstNode node) {
		if (node.isOperation()) {
			if (isExpression(vars, node)) {
				IList<AstNode> newChildren = new DoubleLinkedList<AstNode>();
				IList<AstNode> oldChildren = node.getChildren();
				for (int i = 0; i < oldChildren.size(); i++) {
					newChildren.add(simplifyHelper(vars, oldChildren.get(i)));
				}
				return new AstNode(node.getName(), newChildren);
			} else {
				return new AstNode(toDoubleHelper(vars, node));
			}
		} else if (node.isVariable()) {
			if (vars.containsKey(node.getName())) {
				return simplifyHelper(vars, vars.get(node.getName()));
			}
		}
		return node;
	}

	// Pass an Environment Object and an AstNode as parameters. The AstNode
	// contains data we need to plot an expression. The data contains
	// expression which is used to plot the image, variable name,
	// minimum value of the variable, maximum value of the variable and
	// number of increment each time.
	// It will analyze the node and plot the image using with the given data, and it
	// returns a node which contains simplified version of the
	// expression(if possible)
	// throws EvaluationError if any of the expressions contains an
	// undefined variable.
	// throws EvaluationError is minimum value of the variable is
	// larger than the maximum value of the variable
	// throws Evaluation Error if the variable is already defined
	// throws Evaluation Error if the number of increment is
	// zero or negative
	public static AstNode plot(Environment env, AstNode node) {
		AstNode expression = simplify(env, node.getChildren().get(0));
		double lowerBound = toDoubleHelper(env.getVariables(), node.getChildren().get(2));
		double upperBound = toDoubleHelper(env.getVariables(), node.getChildren().get(3));
		double step = toDoubleHelper(env.getVariables(), node.getChildren().get(4));

		String varName = node.getChildren().get(1).getName();
		for (AstNode child : expression.getChildren()) {
			if (!isDefinedVariable(env, child, varName)) {
				throw new EvaluationError("the expression contains an undefined variable");
			}
		}
		if (lowerBound > upperBound) {
			throw new EvaluationError("varMin > varMax");
		} else if (env.getVariables().containsKey(varName)) {
			throw new EvaluationError("'var' was already defined");
		} else if (step <= 0) {
			throw new EvaluationError("step is zero or negative");
		}

		IList<Double> xValues = new DoubleLinkedList<>();
		;
		IList<Double> yValues = new DoubleLinkedList<>();
		;
		for (int i = 0; i <= (getNumericValue(env, node, 3) - getNumericValue(env, node, 2)) / step; i++) {
			double increments = i * step;
			xValues.add(lowerBound + increments);
			env.getVariables().put(varName, new AstNode(xValues.get(i)));
			yValues.add(toDoubleHelper(env.getVariables(), expression));
		}
		// remove the temporary value added during the loop
		env.getVariables().remove(varName);
		env.getImageDrawer().drawScatterPlot("plot", varName, "output", xValues, yValues);
		return expression;
	}

	// Pass an Environment Object, an AstNode and an integer as parameters.
	// Use integer to determine the index of the object we want to get.
	// Return a double based on the index of the node's children
	private static double getNumericValue(Environment env, AstNode node, int index) {
		return toDouble(env, node.getChildren().get(index)).getNumericValue();
	}

	// Pass a IList<> object and an integer as parameters.
	// If the size of IList<> is not equal to the given integer
	// Throws an EvaluationError
	private static void checkNumberOfOperands(IList<AstNode> children, int count) {
		if (children.size() != count) {
			throw new EvaluationError(
					"Given " + String.valueOf(children.size()) + " operands, but expected " + String.valueOf(count));
		}
	}

	// Pass an IDictionary<> object and an AstNode as parameters.
	// Return true if the given node is an expression (contains at least on
	// undefined variable). Otherwise false
	private static boolean isExpression(IDictionary<String, AstNode> variables, AstNode node) {
		if (node.isOperation()) {
			String operationName = node.getName();
			if (operationName.equals("/") || operationName.equals("sin") || operationName.equals("cos")) {
				return true;
			} else {
				for (AstNode child : node.getChildren()) {
					if (isExpression(variables, child)) {
						return true;
					}
				}
				return false;
			}
		} else if (node.isVariable()) {
			if (isConstant(variables, node)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	// Returns true if a variable evaluates to a constant
	// Returns false if the variable evaluates to an expression or is not a variable
	private static boolean isConstant(IDictionary<String, AstNode> vars, AstNode var) {
		if (var.isVariable() && vars.containsKey(var.getName())) {
			AstNode value = vars.get(var.getName());
			if (value.isNumber()) {
				return true;
			} else {
				return !isExpression(vars, value);
			}
		}
		return false;
	}

	// Pass an Environment object, an AstNode and a String as parameters.
	// return false if the given node is not a defined variable and its
	// name does not match the given String. Otherwise return ture.
	private static boolean isDefinedVariable(Environment env, AstNode node, String varName) {
		if (node.isOperation()) {
			boolean result = true;
			for (AstNode child : node.getChildren()) {
				result = isDefinedVariable(env, child, varName);
				if (!result) {
					return result;
				}
			}
		} else if (node.isVariable() && !node.getName().equals(varName)) {
			if (!isConstant(env.getVariables(), node)) {
				return env.getVariables().containsKey(node.getName());
			}
		}
		return true;
	}

	// TODO: Solve function
	/*
	 * public static AstNode solve(Environment env, AstNode node) { return new
	 * AstNode(toSolutionHelper(env, node)); }
	 * 
	 * private static double toSolutionHelper(Environment env, AstNode node) {
	 * String varName = node.getChildren().get(1).getName(); for (AstNode child :
	 * node.getChildren()) { if (isDefined(env, child, varName)) { throw new
	 * EvaluationError("the expression contains an undefined variable"); } } //"="
	 * should be the first node in expression AstNode equalSign =
	 * node.getChildren().get(0).getChildren().get(0);
	 * if(!equalSign.getName().equals("=")) { throw new
	 * EvaluationError("the expression does not contain an equal sign"); }
	 * checkNumberOfOperands(equalSign.getChildren(), 2); AstNode rightSideNodes =
	 * equalSign.getChildren().get(1); equalSign.getChildren().set(1,
	 * equal(equalSign.getChildren().get(0), equalSign.getChildren().get(1),
	 * varName, true, "+", 1)); }
	 * 
	 * 
	 * 
	 * private static AstNode equal(AstNode node1, AstNode node2, String varName,
	 * Boolean isLeftSide, String sign, int level ) {//pls find a better name //need
	 * to solve sin/cos...etc special cases. AstNode baseNode = new AstNode(0);
	 * if(isLeftSide) { if(node2.isVariable() && node2.getName().equals(varName)) {
	 * IList<AstNode> temp = new DoubleLinkedList<>(); //must import
	 * DoubleLinkedList temp.add(baseNode); temp.add(new AstNode(varName)); baseNode
	 * = new AstNode(sign, temp); } else if(node2.isOperation()) { String name =
	 * node2.getName(); IList<AstNode> children = node2.getChildren();
	 * if(name.equals("+")) { baseNode = equal(node2.getChildren().get(0),
	 * node2.getChildren().get(1), varName, ) }
	 * 
	 * } } }
	 */
}
