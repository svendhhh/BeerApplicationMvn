/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sentenceman.beer.beerapplication.web;

import com.sentenceman.beer.beerapplication.ejb.BreweryFacade;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.sentenceman.beer.beerapplication.entities.Brewery;
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
public class BreweryController implements Serializable {
    private static final long serialVersionUID = 1133525233235L;

    private Brewery currentBrewery;

    private DataModel items = null;

    @EJB
    private BreweryFacade ejbFacade;

    private PaginationHelper paginationHelper;

    private int selectedItemIndex;

    public BreweryController() {
        
    }

    public Brewery getSelected() {
        if (currentBrewery == null) {
            currentBrewery = new Brewery();
            selectedItemIndex = -1;
        }
        return currentBrewery;
    }

    private BreweryFacade getFacade() {
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
        currentBrewery = (Brewery)getItems().getRowData();
        selectedItemIndex = paginationHelper.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        currentBrewery = new Brewery();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(currentBrewery);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BreweryCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        currentBrewery = (Brewery)getItems().getRowData();
        selectedItemIndex = paginationHelper.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(currentBrewery);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BreweryUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        currentBrewery = (Brewery)getItems().getRowData();
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
            getFacade().remove(currentBrewery);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("bundle").getString("BreweryDeleted"));
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
            currentBrewery = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
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

    @FacesConverter(forClass=Brewery.class)
    public static class ContactControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BreweryController controller = (BreweryController)facesContext.getApplication().getELResolver().
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
            if (object instanceof Brewery) {
                Brewery brewery = (Brewery) object;
                return getStringKey(brewery.getId());
            } else {
                throw new IllegalArgumentException("object " + 
                        object + 
                        " is of type " + 
                        object.getClass().getName() + 
                        "; expected type: " +
                        BreweryController.class.getName());
            }
        }

    }

}
