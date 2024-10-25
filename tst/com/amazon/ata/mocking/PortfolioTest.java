package com.amazon.ata.mocking;

import com.amazon.stock.InsufficientStockException;
import com.amazon.stock.Stock;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioTest {

    @Test
    void addStocks_addASingleStock_onlyStockPresent() {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Portfolio portfolio = new Portfolio(stocks);
        Stock stockToAdd = new Stock("id", "name");
        Integer quantityToAdd = 1;

        // WHEN
        portfolio.addStocks(stockToAdd, quantityToAdd);

        // THEN
        assertEquals(1, portfolio.getStocks().get(stockToAdd));
    }

    @Test
    void removeStocks_stockNotInPortfolio_throwsInsufficientStockException() throws InsufficientStockException {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        stocks.put(stockInPortfolio, 5);

        Portfolio portfolio = new Portfolio(stocks);

        Stock stockNotInPortfolio = new Stock("id", "name");

        // WHEN + THEN
        assertThrows(InsufficientStockException.class, () -> portfolio.removeStocks(stockNotInPortfolio, 1));
    }

    @Test
    void removeStocks_notEnoughStockInPortfolio_throwsInsufficientStockException() throws InsufficientStockException {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        int quantity = 5;
        stocks.put(stockInPortfolio, quantity);

        Portfolio portfolio = new Portfolio(stocks);

        // WHEN + THEN
        assertThrows(InsufficientStockException.class, () -> portfolio.removeStocks(stockInPortfolio, quantity + 2));
    }

    @Test
    void removeStocks_exactlyEnoughStockInPortfolio_removesFromMap() throws InsufficientStockException {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        int quantity = 5;
        stocks.put(stockInPortfolio, quantity);

        Portfolio portfolio = new Portfolio(stocks);

        // WHEN
        portfolio.removeStocks(stockInPortfolio, quantity);

        // THEN
        assertEquals(0, stocks.get(stockInPortfolio));
    }

    @Test
    void removeStocks_lessStockThanAvailable_remainingStockInPortfolio() throws InsufficientStockException {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        int quantity = 5;
        stocks.put(stockInPortfolio, quantity);

        Portfolio portfolio = new Portfolio(stocks);
        Integer quantityToRemain = 2;

        // WHEN
        portfolio.removeStocks(stockInPortfolio, quantity - quantityToRemain);

        // THEN
        assertEquals(quantityToRemain, stocks.get(stockInPortfolio));
    }

    @Test
    void getStocks_modifyingMapPassedToConstructor_doesntChangeInternalRepresentation() {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        int quantity = 5;
        stocks.put(stockInPortfolio, quantity);

        Portfolio portfolio = new Portfolio(stocks);

        // WHEN
        stocks.put(stockInPortfolio, quantity - 2);

        // THEN
        Map<Stock, Integer> expectedStocks = new HashMap<>();
        expectedStocks.put(stockInPortfolio, quantity);
        assertEquals(expectedStocks, portfolio.getStocks());
    }

    @Test
    void getStocks_portfolioIsWellEncapsulated_returnsCopyOfStocks() {
        // GIVEN
        Map<Stock, Integer> stocks = new HashMap<>();
        Stock stockInPortfolio = new Stock("amzn", "Amazon");
        int quantity = 5;
        stocks.put(stockInPortfolio, quantity);

        Portfolio portfolio = new Portfolio(stocks);

        // WHEN
        Map<Stock, Integer> copyOfStocks = portfolio.getStocks();
        copyOfStocks.put(stockInPortfolio, quantity - 2);

        // THEN
        assertEquals(stocks, portfolio.getStocks());
    }
}
