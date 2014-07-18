/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sentenceman.beer.beerapplication.ejb;

import com.sentenceman.beer.beerapplication.entities.Beer;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Svend
 */
@Stateless
public class BeerFacade extends AbstractFacade<Beer>{
    
    @PersistenceContext(unitName = "BeerApplicationPU")
    private EntityManager em;

    public BeerFacade() {
        super(Beer.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    
}
