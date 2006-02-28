package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignatureImpl;
import org.faktorips.util.ArgumentCheck;


/**
 * Abstract default implementation of FlFunction.
 */
public abstract class AbstractFlFunction extends FunctionSignatureImpl implements FlFunction {
    
    protected ExprCompiler compiler;
    private String description;
    
    /**
     * Creates a new function with a defined argument list.
     */
    public AbstractFlFunction(String name, String description, Datatype type, Datatype[] argTypes) {
        super(name, type, argTypes);
        this.description = description;
    }

    /**
     * Creates a new function signature with a variable argument list.
     * 
     * @param name the name of this function
     * @param description a description of this function
     * @param type the return type of this function
     * @param argType defines the Datatype of the arguments in the variable argument list
     */
    public AbstractFlFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, type, argType);
        this.description = description;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.fl.FlFunction#setCompiler(org.faktorips.fl.ExprCompiler)
     */
    public void setCompiler(ExprCompiler compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.fl.FlFunction#getCompiler()
     */
    public ExprCompiler getCompiler() {
        return compiler;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.fl.FlFunction#getDescription()
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.fl.FlFunction#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
