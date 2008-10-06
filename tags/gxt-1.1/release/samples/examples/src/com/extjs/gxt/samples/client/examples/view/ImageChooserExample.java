/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.extjs.gxt.samples.client.examples.view;

import com.extjs.gxt.samples.client.ExampleServiceAsync;
import com.extjs.gxt.samples.client.examples.model.Photo;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.StoreFilterField;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

public class ImageChooserExample extends LayoutContainer  {

  private ListStore<BeanModel> store;
  private SimpleComboBox<String> sort;
  private XTemplate detailTp;
  private ListView<BeanModel> view;
  private LayoutContainer details;
  private Dialog chooser;
  private Image image;

  @Override
  protected void onRender(Element parent, int index) {
    super.onRender(parent, index);

    detailTp = XTemplate.create(getDetailTemplate());

    final ExampleServiceAsync service = (ExampleServiceAsync) Registry.get("service");

    RpcProxy proxy = new RpcProxy() {
      @Override
      protected void load(Object loadConfig, AsyncCallback callback) {
        service.getPhotos(callback);
      }
    };

    ListLoader loader = new BaseListLoader(proxy, new BeanModelReader());
    store = new ListStore<BeanModel>(loader);
    loader.load();

    chooser = new Dialog();
    chooser.setId("img-chooser-dlg");
    chooser.setHeading("Choose an Image");
    chooser.setMinWidth(500);
    chooser.setMinHeight(300);
    chooser.setModal(true);
    chooser.setLayout(new BorderLayout());
    chooser.setBodyStyle("border: none;background: none");
    chooser.setBodyBorder(false);
    chooser.setButtons(Dialog.OKCANCEL);
    chooser.setHideOnButtonClick(true);
    chooser.addListener(Events.Hide, new Listener<WindowEvent>() {
      public void handleEvent(WindowEvent be) {
        BeanModel model = view.getSelectionModel().getSelectedItem();
        Photo photo = model.getBean();
        if (be.buttonClicked == chooser.getButtonById("ok")) {
          image.setUrl(photo.getPath());
          image.setVisible(true);
        }
      }
    });

    ContentPanel main = new ContentPanel();
    main.setBorders(true);
    main.setBodyBorder(false);
    main.setLayout(new FitLayout());
    main.setHeaderVisible(false);

    ToolBar bar = new ToolBar();
    bar.add(new LabelToolItem("Filter:"));

    StoreFilterField<BeanModel> field = new StoreFilterField<BeanModel>() {
      @Override
      protected boolean doSelect(Store<BeanModel> store, BeanModel parent,
        BeanModel record, String property, String filter) {
        Photo photo = record.getBean();
        String name = photo.getName().toLowerCase();
        if (name.indexOf(filter.toLowerCase()) != -1) {
          return true;
        }
        return false;
      }

      @Override
      protected void onFilter() {
        super.onFilter();
        view.getSelectionModel().select(0);
      }

    };
    field.setWidth(100);
    field.bind(store);

    bar.add(new AdapterToolItem(field));
    bar.add(new SeparatorToolItem());
    bar.add(new LabelToolItem("Sort By:"));

    sort = new SimpleComboBox<String>();
    sort.setTriggerAction(TriggerAction.ALL);
    sort.setEditable(false);
    sort.setForceSelection(true);
    sort.setWidth(90);
    sort.add("Name");
    sort.add("File Size");
    sort.add("Last Modified");
    sort.setSimpleValue("Name");
    sort.addListener(Events.Change, new Listener<FieldEvent>() {
      public void handleEvent(FieldEvent be) {
        sort();
      }
    });

    bar.add(new AdapterToolItem(sort));

    main.setTopComponent(bar);

    view = new ListView<BeanModel>() {
      @Override
      protected BeanModel prepareData(BeanModel model) {
        Photo photo = model.getBean();
        long size = photo.getSize() / 1000;
        model.set("shortName", Util.ellipse(photo.getName(), 15));
        model.set("sizeString", NumberFormat.getFormat("#0").format(size) + "k");
        model.set("dateString", DateTimeFormat.getMediumDateTimeFormat().format(
            photo.getDate()));
        return model;
      }
    };
    view.setId("img-chooser-view");
    view.setTemplate(getTemplate());
    view.setBorders(false);
    view.setStore(store);
    view.setItemSelector("div.thumb-wrap");
    view.setOverStyle("x-view-over");
    view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    view.getSelectionModel().addListener(Events.SelectionChange,
        new Listener<SelectionEvent<BeanModel>>() {
          public void handleEvent(SelectionEvent<BeanModel> be) {
            onSelectionChange(be);
          }
        });
    main.add(view);

    details = new LayoutContainer();
    details.setBorders(true);
    details.setStyleAttribute("backgroundColor", "white");

    BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 150, 150, 250);
    eastData.setSplit(true);

    BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
    centerData.setMargins(new Margins(0, 5, 0, 0));

    chooser.add(main, centerData);
    chooser.add(details, eastData);

    setLayout(new FlowLayout(10));
    add(new Button("Choose", new SelectionListener<ButtonEvent>() {
      @Override
      public void componentSelected(ButtonEvent ce) {
        chooser.show();
        view.getSelectionModel().select(0);
      }
    }));
    
    image = new Image();
    image.getElement().getStyle().setProperty("marginTop", "10px");
    image.setVisible(false);
    add(image);
  }

  private void sort() {
    String v = sort.getSimpleValue();
    if (v.equals("Name")) {
      store.sort("name", SortDir.ASC);
    } else if (v.equals("File Size")) {
      store.sort("size", SortDir.ASC);
    } else {
      store.sort("date", SortDir.ASC);
    }
  }

  private void onSelectionChange(SelectionEvent<BeanModel> se) {
    if (se.selection.size() > 0) {
      detailTp.overwrite(details.getElement(), Util.getJsObject(se.selection.get(0)));
      chooser.getButtonById("ok").enable();
    } else {
      chooser.getButtonById("ok").disable();
      details.el().setInnerHtml("");
    }
  }

  private native String getTemplate() /*-{
   return ['<tpl for=".">',
   '<div class="thumb-wrap" id="{name}">',
   '<div class="thumb"><img src="{path}" title="{name}"></div>',
   '<span>{shortName}</span></div>',
   '</tpl>'].join("");
   }-*/;

  public native String getDetailTemplate() /*-{
   return ['<div class="details">',
   '<tpl for=".">',
   '<img src="{path}"><div class="details-info">',
   '<b>Image Name:</b>',
   '<span>{name}</span>',
   '<b>Size:</b>',
   '<span>{sizeString}</span>',
   '<b>Last Modified:</b>',
   '<span>{dateString}</span></div>',
   '</tpl>',
   '</div>'].join("");
   }-*/;
}
