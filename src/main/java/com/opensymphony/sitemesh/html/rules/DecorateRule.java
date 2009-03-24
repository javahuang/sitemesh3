package com.opensymphony.sitemesh.html.rules;

import com.opensymphony.sitemesh.Context;
import com.opensymphony.sitemesh.InMemoryContent;
import com.opensymphony.sitemesh.tagprocessor.BasicBlockRule;
import com.opensymphony.sitemesh.tagprocessor.Tag;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Rule that applies decorators to inline blocks of content.
 *
 * <ul>
 * <li>A {@link com.opensymphony.sitemesh.Content} object will be created for the inline block.</li>
 * <li>The contents of the tag body will be exposed as the <code>body</code> property.</li>
 * <li>All attributes of the tag will be copied as named properties (see example below).</li>
 * <li>The <code>decorator</code> attribute will specify which decorator is used.</li>
 * </ul>
 *
 * <h3>Example</h3>
 *
 * <pre>Some content &lt;decorate decorator='/mydecorator' title='foo' cheese='bar'&gt;blah&lt;/decorate&gt;</pre>
 *
 * <p>This will apply the decorator named <code>/mydecorator</code>, passing in {@link com.opensymphony.sitemesh.Content}
 * with the following properties:</p>
 * <pre>
 * body=blah
 * title=foo
 * cheese=bar
 * </pre>
 *
 * @author Joe Walnes
 */
public class DecorateRule extends BasicBlockRule<InMemoryContent> {

    private final Context siteMeshContext;

    public DecorateRule(Context siteMeshContext) {
        super("decorate");
        this.siteMeshContext = siteMeshContext;
    }

    @Override
    protected InMemoryContent processStart(Tag tag) throws IOException {
        context.pushBuffer();

        InMemoryContent content = new InMemoryContent();

        for (int i = 0, count = tag.getAttributeCount(); i < count; i++) {
            content.addProperty(
                    tag.getAttributeName(i),
                    tag.getAttributeValue(i));
        }

        return content;
    }

    @Override
    protected void processEnd(Tag tag, InMemoryContent content) throws IOException {
        CharSequence body = context.currentBufferContents();
        context.popBuffer();

        content.setOriginal(body);
        content.addProperty("body", body);

        String decoratorName = content.getProperty("decorator").value();
        StringWriter writer = new StringWriter();
        boolean applied = siteMeshContext.applyDecorator(decoratorName, content, writer);
        if (applied) {
            context.currentBuffer().append(writer.toString());
        } else {
            context.currentBuffer().append(body);
        }
    }

}