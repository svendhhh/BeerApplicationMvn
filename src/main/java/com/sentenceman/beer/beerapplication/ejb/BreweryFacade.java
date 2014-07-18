/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sentenceman.beer.beerapplication.ejb;

import com.sentenceman.beer.beerapplication.entities.Brewery;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Svend
 */
@Stateless
public class BreweryFacade extends AbstractFacade<Brewery>{
    
    @PersistenceContext(unitName = "BeerApplicationPU")
    private EntityManager em;

    public BreweryFacade() {
        super(Brewery.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    
}
