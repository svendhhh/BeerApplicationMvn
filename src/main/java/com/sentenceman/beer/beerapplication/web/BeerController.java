/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sentenceman.beer.beerapplication.web;

import com.sentenceman.beer.beerapplication.ejb.BeerFacade;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.sentenceman.beer.beerapplication.entities.Beer;
import com.sentenceman.beer.beerapplication.web.util.JsfUtil;
import com.sentenceman.beer.beerapplication.web.util.PaginationHelper;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

/**
 *
 * @author Svend
 */
@Named
@SessionScoped
public class BeerController implements Serializable {
    private static final long serialVersionUID = 1133525233235L;

    private Beer currentBeer;

    private DataModel items = null;

    @EJB
    private BeerFacade ejbFacade;

    private PaginationHelper paginationHelper;

    private int selectedItemIndex;

    public BeerController() {
        
    }

    public Beer getSelected() {
        if (currentBeer == null) {
            currentBeer = new Beer();
            selectedItemIndex = -1;
        }
        return currentBeer;
    }

    private BeerFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = new PaginationHelper(10) {
                
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }
                
                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[] { getPageFirstItem(), getPageFirstItem() + getPageSize() }));
                }
            };
        }
        return paginationHelper;
    }

    /* Cut and paste from here on: */
    
    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        currentBeer = (Beer)getItems().getRowData();
        selectedItemIndex = paginationHelper.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        currentBeer = new Beer();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(currentBeer);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BeerCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        currentBeer = (Beer)getItems().getRowData();
        selectedItemIndex = paginationHelper.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(currentBeer);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BeerUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        currentBeer = (Beer)getItems().getRowData();
        selectedItemIndex = paginationHelper.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(currentBeer);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BeerDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (paginationHelper.getPageFirstItem() >= count) {
                paginationHelper.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            currentBeer = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPaginationHelper().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPaginationHelper().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPaginationHelper().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=Beer.class)
    public static class ContactControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BeerController controller = (BeerController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "contactController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Beer) {
                Beer beer = (Beer) object;
                return getStringKey(beer.getId());
            } else {
                throw new IllegalArgumentException("object " + 
                        object + 
                        " is of type " + 
                        object.getClass().getName() + 
                        "; expected type: " +
                        BeerController.class.getName());
            }
        }

    }

}
