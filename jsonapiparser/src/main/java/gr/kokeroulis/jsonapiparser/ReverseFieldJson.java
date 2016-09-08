package gr.kokeroulis.jsonapiparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* What is this?
 * Sometimes we might have the same element inside the attributes and the included.
 * So this annotation is here to help us solve that issue.
 * E.x. we have the attributes Foo and Bar. And Bar has a relationship with name "Foo"
 * which has nothing to do with the "Foo" attribute. So we use this annotation to rename
 * the "Foo" attribute to "BAZ".
 *
 * use it like this
 *
 * public class SomePojo {
 *
 *    @ReverseFieldJson(name = "foo")
 *    public Foo baz;
 *
 *    @Relationship(type = "some_relationship_key")
 *    public Bar foo;
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface ReverseFieldJson {
    String name();
}