package org.webharvest.gui.settings.validation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;
import org.webharvest.definition.validation.SchemaComponentFactory;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.definition.validation.SchemaResolverPostProcessor;
import org.webharvest.definition.validation.SchemaSource;
import org.webharvest.definition.validation.TransformerPair;
import org.webharvest.definition.validation.URIToSchemaSourceTransformer;
import org.webharvest.gui.GuiUtils;
import org.webharvest.gui.settings.validation.XmlSchemasView.Presenter;

/**
 * Default implementation of MVP's {@link XmlSchemasView.Presenter} interface
 * which also realizes {@link SchemaResolverPostProcessor} interface in order to
 * register {@link SchemaSource} retrieved form contained XML schema DTOs .
 *
 * @see XmlSchemasView.Presenter
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class XmlSchemasPresenter implements Presenter,
        SchemaResolverPostProcessor {

    private Set<XmlSchemaDTO> schemaDTOs = new HashSet<XmlSchemaDTO>();

    private final SchemaResolver schemaResolver =
        SchemaComponentFactory.getSchemaResolver();

    private final Transformer<String, SchemaSource> transformer =
        new TransformerPair<String, URI, SchemaSource>(
                new FilePathToURITransformer(),
                new URIToSchemaSourceTransformer());

    private final XmlSchemasView view;

    /**
     * Presenter's constructor accepting not-{@code null} reference to the
     * instance of {@link XmlSchemasView} with which this presenter is intended
     * to cooperate (MVP design pattern).
     *
     * @param view
     *            not-{@code null} reference to the {@link XmlSchemasView}
     *            instance.
     */
    public XmlSchemasPresenter(final XmlSchemasView view) {
        if (view == null) {
            throw new IllegalArgumentException("View must not be null.");
        }
        this.view = view;
        // register itself in SchemaResolver
        this.schemaResolver.addPostProcessor(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerSchema(final XmlSchemaDTO schema) {
        if (schema == null) {
            throw new IllegalArgumentException("DTO must not be null.");
        }
        if (schemaDTOs.add(schema)) {
            schemaResolver.refresh();
            view.addToList(schema);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterSchema(final XmlSchemaDTO schema) {
        if (schema == null) {
            throw new IllegalArgumentException("DTO must not be null.");
        }
        if (schemaDTOs.remove(schema)) {
            schemaResolver.refresh();
            view.removeFromList(schema);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerSchemas(final Set<XmlSchemaDTO> schemas) {
        if (schemas == null) {
            throw new IllegalArgumentException(
                    "Collection of DTOs must not be null.");
        }
        if (schemaDTOs.addAll(schemas)) {
            schemaResolver.refresh();
            for (XmlSchemaDTO schema : schemas) {
                view.addToList(schema);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcess(final SchemaResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException(
                    "SchemaResolver must not be null.");
        }
        for (final XmlSchemaDTO schema : schemaDTOs) {
            try {
                resolver.registerSchemaSource(transformer.transform(schema
                        .getLocation()));
            } catch (final TransformationException e) {
                GuiUtils.showWarningMessage(
                        "There was a problem with loading following XML schema:"
                        + schema.getLocation());
            }
        }
    }

}
