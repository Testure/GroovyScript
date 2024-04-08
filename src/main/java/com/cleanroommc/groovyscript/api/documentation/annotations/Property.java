package com.cleanroommc.groovyscript.api.documentation.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * Functions in one of three ways depending on what the annotation is attached to:
 * <ul>
 *     <li>
 *         {@link ElementType#FIELD}: Marks the target field with this {@link Property}. {@link #property()} must be either set to the field name or unset.
 *         Can only allow one annotation per field.
 *     </li>
 *     <li>
 *         {@link ElementType#TYPE}: Marks the field targeted by {@link #property()} within the attached class with this {@link Property}.
 *         Multiple will be wrapped in {@link Properties}.
 *     </li>
 *     <li>
 *         {@link ElementType#METHOD}: Marks the field targeted by {@link #property()} within the class the attached method returns with this {@link Property}.
 *         Can only be attached via being inside {@link RecipeBuilderDescription#requirement()}.
 *     </li>
 * </ul>
 * <p>
 * Elements:
 * <ul>
 *     <li>{@link #value()} is a localization key that is autogenerated to be
 *     <code>
 *         groovyscript.wiki.{@link com.cleanroommc.groovyscript.compat.mods.GroovyContainer#getModId() GroovyContainer#getModId()}.{@link Field#getName()}.value
 *     </code>
 *     and states what the property does.
 *     </li>
 *     <li>{@link #property()} either contains nothing if {@link Property} was created attached to a field, or the relevant {@link Field#getName()} string.</li>
 *     <li>{@link #defaultValue()} a string containing the default value of the property. If empty, defaults to {@code null}.</li>
 *     <li>{@link #valid()} is an array of {@link Comp} that indicates the requirements of the {@link Property} to pass validation.</li>
 *     <li>{@link #requirement()} is a localization key that states the requirements for the property to pass validation provided the requirements are too
 *     complex to represent via {@link #valid()}.</li>
 *     <li>{@link #ignoresInheritedMethods()} if this {@link Property} annotation requires any methods targeting the {@link Property} to not be inherited methods.</li>
 *     <li>{@link #needsOverride()} if this {@link Property} annotation needs another {@link Property} annotation with this element set to {@code true} to function.
 *     Used in wrapper classes, such as {@link com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder AbstractRecipeBuilder}, where some or all of the fields may not be needed in subclasses.</li>
 *     <li>{@link #hierarchy()} is an integer that controls the precedence of the {@link Property} annotation when multiple versions of it exist for a single field.
 *     A lower hierarchy overrides a higher one, with the default having a value of 10.</li>
 *     <li>{@link #priority()} is an integer that influences the sorting of the {@link Property} relative to other {@link Property Properties}.
 *     Should be set to a higher value on {@link Property Properties} that have {@link #needsOverride()} set to {@code true}, and lower on any property added via
 *     {@link ElementType#METHOD} to ensure proper prioritization.</li>
 * </ul>
 *
 * @see Properties
 */
@Repeatable(Property.Properties.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Property {

    /**
     * The localization key for the name of the compat, will default to generating
     * <code>
     * groovyscript.wiki.{@link com.cleanroommc.groovyscript.compat.mods.GroovyContainer#getModId() GroovyContainer#getModId()}.{@link com.cleanroommc.groovyscript.registry.VirtualizedRegistry#getName() VirtualizedRegistry#getName()}.{@link Field#getName()}.value
     * </code>
     *
     * @return localization key for what the target field will accomplish
     */
    String value() default "";

    /**
     * If this {@link Property} annotation is attached to a field, this element is set to the name of the field they are attached to.
     * When annotated on a field already, this should not be set or be set to the field name. Any other string will generate a warning.
     *
     * @return the target property, if not annotated to a field.
     */
    String property() default "";

    /**
     * The default value of the property. Only needs to be declared if the default value is different from the default value created by a new object of that type.
     * For instance, "0" is the default value for {@code int}s, and so does not have to be declared.
     *
     * @return what the default value is, defaults to {@code false}, {@code 0}, {@code 0.0f}, {@code "null"}, etc depending on the property class.
     * @see com.cleanroommc.groovyscript.documentation.Builder#defaultValueConverter
     */
    @SuppressWarnings("JavadocReference")
    String defaultValue() default "";

    /**
     * The primary way to document properties, supplemented by {@link #requirement()}.
     * The three main ways this element is used is to refer to:
     * <br>- a number: Would indicate comparing directly against the number.
     * <br>- an array or list: Would indicate comparing against the length of the array/list.
     * <br>- another object: Would use to indicate {@code not null} is required.
     *
     * <table>
     *  <tr>
     *   <th>validation</th>
     *   <th>code</th>
     *  </tr>
     *  <tr>
     *   <td><code>x == 1</code></td>
     *   <td><code>valid = @Comp("1")</code></td>
     *  </tr>
     *  <tr>
     *   <td><code>x != 1</code></td>
     *   <td><code>valid = @Comp(value = "1", type = Comp.Type.NOT)</code></td>
     *  </tr>
     *  <tr>
     *   <td><code>x != null</code></td>
     *   <td><code>valid = @Comp(value = "null", type = Comp.Type.NOT)</code></td>
     *  </tr>
     *  <tr>
     *   <td><code>x > 0</code></td>
     *   <td><code>valid = @Comp(value = "0", type = Comp.Type.GT)</code></td>
     *  </tr>
     *  <tr>
     *   <td><code>x >= 0 && x <= 5</code></td>
     *   <td><code>valid = {{@literal @}Comp(value = "0", type = Comp.Type.GTE), @Comp(value = "5", type = Comp.Type.LTE)}</code></td>
     *  </tr>
     * </table>
     *
     * @return an array of {@link Comp} entries indicating valid values for the property to be.
     */
    Comp[] valid() default {};

    /**
     * A localization key to declare validation requirements that are too complex to represent in {@link #valid()}.
     *
     * @return a string describing the valid value(s) for the field to be to pass validation
     */
    String requirement() default "";

    /**
     * Controls if the property ignores any methods targeting the property that are inherited from a parent class.
     *
     * @return if the property should ignore any methods inherited from a parent class, defaults to {@code false}
     */
    boolean ignoresInheritedMethods() default false;

    /**
     * Controls if the property needs an overriding property to enable it. Used in wrapper classes, such as {@link com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder AbstractRecipeBuilderr}, where some or all of the fields
     * may not be needed in subclasses.
     * This can also be used to effectively disable properties from being documented by attaching it to the lowest hierarchy.
     * The annotation for the given property field with the lowest {@link #hierarchy} score must have this be {@code false} for the property to be documented.
     *
     * @return if the property needs an overriding annotation to enable it, defaults to {@code false}
     */
    boolean needsOverride() default false;

    /**
     * Hierarchy of the property, relative to other properties applying to the same field.
     *
     * @return the property hierarchy (where lower overrides hider)
     */
    int hierarchy() default 10;

    /**
     * Priority of the property, relative to other properties applied to the Recipe Builder.
     * Priorities sort entries such that lowest is first.
     *
     * @return the property priority (relative to other properties for the same Recipe Builder)
     */
    int priority() default 1000;


    /**
     * Wrapper to allow repeatable instances of {@link Property}.
     * If more than one {@link Property} is applied to anywhere other than a class, it will generate an error.
     * For a given Field. only a single {@link Property} should be attached,
     * and for a given Method, all {@link Property} annotations should be placed inside {@link RecipeBuilderDescription#requirement()}
     *
     * @see Property
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Properties {

        Property[] value();

    }

}
