package com.org.ddd.utils.paging;

public class Page<E> {
    private Iterable<E> elementsOnPage;
    private int totalNumerOfElements;

    public Page(Iterable<E> elementsOnPage, int totalNumerOfElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumerOfElements = totalNumerOfElements;
    }

    public Iterable<E> getElementsOnPage() {
        return elementsOnPage;
    }

    public int getTotalNumerOfElements() {
        return totalNumerOfElements;
    }
}
