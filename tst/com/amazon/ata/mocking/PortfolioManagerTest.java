package com.amazon.ata.mocking;

import com.amazon.stock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class PortfolioManagerTest {
    private Stock amznStock = new Stock("amzn", "Amazon");
    private BigDecimal currentAmazonStockPrice = BigDecimal.valueOf(1_000L);
    private int quantityInPortfolio = 3;
    private Map<Stock, Integer> portfolioStocks;

    private Stock nonExistentStock = new Stock("id", "name");

    @Mock
    private Portfolio portfolio;

    @Mock
    private StockExchangeClient client;

    @InjectMocks
    private PortfolioManager portfolioManager;

    @BeforeEach
    void setUp() {
        portfolioStocks = new HashMap<>();
        portfolioStocks.put(amznStock, quantityInPortfolio);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMarketValue_portfolioWithStocks_returnsValueOfStocks() {
        // GIVEN
        BigDecimal expectedValue = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityInPortfolio));

        // WHEN
        when(portfolio.getStocks()).thenReturn(portfolioStocks);
        when(client.getPrice(amznStock)).thenReturn(currentAmazonStockPrice);

        BigDecimal value = portfolioManager.getMarketValue();

        // THEN
        assertEquals(expectedValue, value);
    }

    @Test
    void buy_existingStock_returnsCostOfBuyingStock() throws NonExistentStockException {
        // GIVEN
        int quantityToBuy = 5;
        BigDecimal expectedCost = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityToBuy));
        BuyStockRequest request = BuyStockRequest.builder()
                .withSymbol(amznStock.getSymbol())
                .withQuantity(quantityToBuy)
                .build();

        BuyStockResponse response = BuyStockResponse.builder()
                .withSymbol(request.getSymbol())
                .withPrice(currentAmazonStockPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
                .withQuantity(request.getQuantity())
                .build();

        // WHEN

        when(client.submitBuy(amznStock, quantityToBuy)).thenReturn(response.getPrice());

        BigDecimal cost = portfolioManager.buy(amznStock, quantityToBuy);

        // THEN
        assertEquals(expectedCost, cost);
    }

    @Test
    void buy_nonExistingStock_returnsNull() {
        // GIVEN
        int quantityToBuy = 5;

        BuyStockResponse response = BuyStockResponse.builder()
                .withSymbol(nonExistentStock.getSymbol())
                .build();

        // WHEN

        when(client.submitBuy(nonExistentStock, quantityToBuy)).thenReturn(response.getPrice());

        BigDecimal cost = portfolioManager.buy(nonExistentStock, quantityToBuy);

        // THEN
        assertNull(cost);
    }

    @Test
    void sell_enoughStocksToSell_returnValueSoldFor() {
        // GIVEN
        int quantityToSell = quantityInPortfolio - 1;
        BigDecimal expectedValue = currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityToSell));

        SellStockResponse response = SellStockResponse.builder()
                .withSymbol(amznStock.getSymbol())
                .withPrice(currentAmazonStockPrice.multiply(BigDecimal.valueOf(quantityToSell)))
                .withQuantity(quantityToSell)
                .build();

        // WHEN

        when(client.submitSell(amznStock, quantityToSell)).thenReturn(response.getPrice());

        BigDecimal value = portfolioManager.sell(amznStock, quantityToSell);

        // THEN
        assertEquals(expectedValue, value);
    }

    @Test
    void sell_notEnoughStocksToSell_returnZeroValue() throws InsufficientStockException {
        // GIVEN
        int quantityToSell = quantityInPortfolio + 1;

        // WHEN

        doThrow(new InsufficientStockException()).when(portfolio).removeStocks(amznStock, quantityToSell);

        BigDecimal value = portfolioManager.sell(amznStock, quantityToSell);

        // THEN
        assertEquals(BigDecimal.ZERO, value);
    }
}
