package calculator.ast;

import calculator.interpreter.Environment;
import calculator.errors.EvaluationError;
import datastructures.concrete.DoubleLinkedList;
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
			} else if (name.equals("toDouble")) {
				checkNumberOfOperands(children, 1);
				return toDoubleHelper(variables, simplifyHelper(variables, node.getChildren().get(0)));
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
		if (node.isOperation()) {
			String operation = node.getName();
			if(operation.equals("simplify")) {
				checkNumberOfOperands(node.getChildren(), 1);
				return simplifyHelper(vars, node.getChildren().get(0));
			} else {
				return simplifyHelper(vars, node);
			}
		} else {
			return simplifyHelper(vars, node);
		}
	}

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
		} else if(node.isVariable()) {
			if(vars.containsKey(node.getName())) {
				return simplifyHelper(vars, vars.get(node.getName()));
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
		
		AstNode expression = simplify(env, node.getChildren().get(0));
		double lowerBound = node.getChildren().get(2).getNumericValue();
		double upperBound = node.getChildren().get(3).getNumericValue();
		double step = node.getChildren().get(4).getNumericValue();
		
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
		
		IList<Double> xValues = new DoubleLinkedList<>();; 
		IList<Double> yValues = new DoubleLinkedList<>();;
		for (int i = 0; i <= (getNumericValue(env, node, 3) - getNumericValue(env, node, 2))
				/ step; i++) {
			double increments = i * step;
			xValues.add(lowerBound + increments);
			env.getVariables().put(varName, new AstNode(xValues.get(i)));
			yValues.add(toDoubleHelper(env.getVariables(), expression));
		}
		env.getVariables().remove(varName); // remove the value added during the loop
		env.getImageDrawer().drawScatterPlot("plot", varName, "output", xValues, yValues);
		return expression;
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
			if (operationName.equals("/") || operationName.equals("sin") || operationName.equals("cos")) {
				return true;
			} else {
				for (AstNode child : node.getChildren()) {
					if(isExpression(variables, child)) {
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
	/*
	 * Returns true if a variable evaluates to a constant
	 * Returns false if the variable evaluates to an expression or is not a variable
	 */
	private static boolean isConstant(IDictionary<String, AstNode> vars, AstNode var) {
		if(var.isVariable() && vars.containsKey(var.getName())) {
			AstNode value = vars.get(var.getName());
			if(value.isNumber()) {
				return true;
			} else {
				return !isExpression(vars, value);
			}
		}
		return false;
	}
	

	private static boolean isDefinedVariable(Environment env, AstNode node, String varName) {
		if (node.isOperation()) {
			boolean result = true;
			for (AstNode child : node.getChildren()) {
				result = isDefinedVariable(env, child, varName);
				if (!result) {
					return result;
				}
			}
			
		}else if (node.isVariable() && !node.getName().equals(varName)) {
			if(!isConstant(env.getVariables(), node)) {
				return env.getVariables().containsKey(node.getName());
			}
		}
		return true;
	}
	
	//TODO: Solve function
	/*
	public static AstNode solve(Environment env, AstNode node) {
		return new AstNode(toSolutionHelper(env, node));
	}

	private static double toSolutionHelper(Environment env, AstNode node) {
		String varName = node.getChildren().get(1).getName();
		for (AstNode child : node.getChildren()) {
			if (isDefined(env, child, varName)) {
				throw new EvaluationError("the expression contains an undefined variable");
			}
		}
		//"=" should be the first node in expression
		AstNode equalSign = node.getChildren().get(0).getChildren().get(0);
		if(!equalSign.getName().equals("=")) {
			throw new EvaluationError("the expression does not contain an equal sign");
		}
		checkNumberOfOperands(equalSign.getChildren(), 2);
		AstNode rightSideNodes = equalSign.getChildren().get(1);
		equalSign.getChildren().set(1, equal(equalSign.getChildren().get(0), equalSign.getChildren().get(1), varName, true, "+", 1)); 
	}
	
	

	private static AstNode equal(AstNode node1, AstNode node2, String varName, Boolean isLeftSide, String sign, int level ) {//pls find a better name
		//need to solve sin/cos...etc special cases. 
		AstNode baseNode = new AstNode(0);
		if(isLeftSide) {
			if(node2.isVariable() && node2.getName().equals(varName)) {
				IList<AstNode> temp = new DoubleLinkedList<>(); //must import DoubleLinkedList
				temp.add(baseNode);
				temp.add(new	 AstNode(varName));
				baseNode = new AstNode(sign, temp);
			} else if(node2.isOperation()) {
				String name = node2.getName();
				IList<AstNode> children = node2.getChildren();
				if(name.equals("+")) {
					baseNode = equal(node2.getChildren().get(0), node2.getChildren().get(1), varName, )
				}
				
			}
		}
	}	
	*/
}
