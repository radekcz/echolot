/*
 * This file (SuggestAreaPeer.java) is part of the Echolot Project (hereinafter "Echolot").
 * Copyright (C) 2008-2018 eXXcellent Solutions GmbH.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 */

package de.exxcellent.echolot.webcontainer.sync.component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import de.exxcellent.echolot.SharedService;
import de.exxcellent.echolot.app.SuggestArea;
import de.exxcellent.echolot.model.SuggestItem;
import de.exxcellent.echolot.model.SuggestModel;
import nextapp.echo.app.Component;
import nextapp.echo.app.util.Context;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;
import nextapp.echo.webcontainer.sync.component.TextComponentPeer;

/**
 * Peer-Class for SuggestArea
 *
 * @author Ralf Enderle
 * @version 1.0
 */
public class SuggestAreaPeer extends TextComponentPeer {

    // JQUERY UI Core
    public static final Service JQUERY_UI_CORE_SERVICE;

    // JQUERY UI Libraries
    public static final Service JQUERY_UI_WIDGET_SERVICE;
    public static final Service JQUERY_UI_POSITION_SERVICE;
    public static final Service JQUERY_UI_AUTOCOMPLETE_SERVICE;

    // SuggestArea
    public static final Service SUGGESTAREA_SYNC_SERVICE;

    /**
     * The serializer used to serialize model instances.
     */
    protected static final XStream xstream;

    static {
        // JQUERY
        JQUERY_UI_CORE_SERVICE = JavaScriptService.forResource("jquery.ui.core", "js/jquery/ui/jquery.ui.core.js");
        JQUERY_UI_WIDGET_SERVICE = JavaScriptService.forResource("jquery.ui.widget", "js/jquery/ui/jquery.ui.widget.js");
        JQUERY_UI_POSITION_SERVICE = JavaScriptService.forResource("jquery.ui.position", "js/jquery/ui/jquery.ui.position.js");
        JQUERY_UI_AUTOCOMPLETE_SERVICE = JavaScriptService.forResource("jquery.ui.autocomplete", "js/suggest/jquery.ui.autocomplete.js");

        // PieChart
        SUGGESTAREA_SYNC_SERVICE = JavaScriptService.forResource("exxcellent.SuggestArea.Sync",
                "js/Sync.SuggestArea.js");

        /* Register JavaScriptService with the global service registry.*/
        WebContainerServlet.getServiceRegistry().add(JQUERY_UI_CORE_SERVICE);
        WebContainerServlet.getServiceRegistry().add(JQUERY_UI_WIDGET_SERVICE);
        WebContainerServlet.getServiceRegistry().add(JQUERY_UI_POSITION_SERVICE);
        WebContainerServlet.getServiceRegistry().add(JQUERY_UI_AUTOCOMPLETE_SERVICE);

        WebContainerServlet.getServiceRegistry().add(SUGGESTAREA_SYNC_SERVICE);

        /* JSON Stream Driver */
        xstream = new XStream(new JsonHierarchicalStreamDriver());

        xstream.alias("suggestItem", SuggestItem.class);
        xstream.alias("suggestModel", SuggestModel.class);

        xstream.processAnnotations(SuggestModel.class);
        xstream.processAnnotations(SuggestItem.class);

    }

    public SuggestAreaPeer() {
        super();
        // Event for SERVER_FILTER
        addEvent(new EventPeer(SuggestArea.INPUT_TRIGGER_SERVER_FILTER,
                SuggestArea.SERVER_FILTER_CHANGED_PROPERTY, String.class) {
            @Override
            public boolean hasListeners(Context context, Component c) {
                return ((SuggestArea) c).hasServerFilterListener();
            }
        });

        // Event for SUGGEST_ITEM_SELECTED
        addEvent(new EventPeer(SuggestArea.INPUT_SUGGEST_ITEM_SELECTED,
                SuggestArea.SUGGESTITEMSELECT_LISTENER_CHANGED_PROPERTY, Integer.class) {
            @Override
            public boolean hasListeners(Context context, Component c) {
                return ((SuggestArea) c).hasSuggestItemSelectListener();
            }
        });
    }

    /**
     * Returns the clientComponentType
     *
     * @param shortType
     * @return
     */
    public String getClientComponentType(boolean shortType) {
        // Return client-side component type name.
        return "exxcellent.SuggestArea";
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class getComponentClass() {
        // Return server-side Java class.
        return SuggestArea.class;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void init(Context context, Component component) {
        super.init(context, component);
        // Obtain outgoing 'ServerMessage' for initial render.
        final ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
        serverMessage.addLibrary(SharedService.ECHOCOMPONENTS_SERVICE.getId());
        serverMessage.addLibrary(SharedService.JQUERY_SERVICE.getId());

        // JQUERY UI
        serverMessage.addLibrary(JQUERY_UI_CORE_SERVICE.getId());

        // JQUERY UI Libraries
        serverMessage.addLibrary(JQUERY_UI_WIDGET_SERVICE.getId());
        serverMessage.addLibrary(JQUERY_UI_POSITION_SERVICE.getId());
        serverMessage.addLibrary(JQUERY_UI_AUTOCOMPLETE_SERVICE.getId());

        // Add SUGGESTAREA JavaScript library to client.
        serverMessage.addLibrary(SUGGESTAREA_SYNC_SERVICE.getId());
    }

    /**
     * Over-ridden to handle request of tag instances are that are serialised
     * as a JSON stucture.
     *
     * @see nextapp.echo.webcontainer.ComponentSynchronizePeer#getOutputProperty(nextapp.echo.app.util.Context, nextapp.echo.app.Component, String, int)
     */
    @Override
    public Object getOutputProperty(final Context context,
                                    final Component component, final String propertyName,
                                    final int propertyIndex) {

        if (SuggestArea.PROPERTY_SUGGEST_MODEL.equals(propertyName)) {
            return xstream.toXML(((SuggestArea) component).getSuggestModel());
        }
        return super.getOutputProperty(context, component, propertyName, propertyIndex);
    }

}
