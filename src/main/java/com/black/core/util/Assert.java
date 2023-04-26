package com.black.core.util;


public class Assert {


    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }



    public static void falseThrows(boolean expression){
        falseThrows(expression, "expression is false");
    }

    public static void falseThrows(boolean expression, String message){
        if (!expression){
            throw new IllegalStateException(message);
        }
    }


    public static void trueThrows(boolean expression){
        trueThrows(expression, "expression is true");
    }

    public static void trueThrows(boolean expression, String message){
        if (expression){
            throw new IllegalStateException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalStateException}
     * if the expression evaluates to {@code false}.
     * @deprecated as of 4.3.7, in favor of {@link #state(boolean, String)}
     */
    @Deprecated
    public static void state(boolean expression) {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * @deprecated as of 4.3.7, in favor of {@link #isTrue(boolean, String)}
     */
    @Deprecated
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Assert that an object is {@code null}.
     * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Assert that an object is {@code null}.
     * @deprecated as of 4.3.7, in favor of {@link #isNull(Object, String)}
     */
    @Deprecated
    public static void isNull( Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void notNull( Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }



    /**
     * Assert that an object is not {@code null}.
     * @deprecated as of 4.3.7, in favor of {@link #notNull(Object, String)}
     */
    @Deprecated
    public static void notNull( Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that the given String is not empty; that is,
     * it must not be {@code null} and not the empty String.
     * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text is empty
     * @see org.springframework.util.StringUtils#hasLength
     */
    public static void hasLength( String text, String message) {
        if (!org.springframework.util.StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Assert that the given String is not empty; that is,
     * it must not be {@code null} and not the empty String.
     * @deprecated as of 4.3.7, in favor of {@link #hasLength(String, String)}
     */
    @Deprecated
    public static void hasLength( String text) {
        hasLength(text,
                "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * Assert that the given String contains valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text does not contain valid text content
     * @see org.springframework.util.StringUtils#hasText
     */
    public static void hasText( String text, String message) {
        if (!org.springframework.util.StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given String contains valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * @deprecated as of 4.3.7, in favor of {@link #hasText(String, String)}
     */
    @Deprecated
    public static void hasText( String text) {
        hasText(text,
                "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }
    
    
}
