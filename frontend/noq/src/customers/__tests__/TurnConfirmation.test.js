import React from 'react'

import { Router } from "react-router-dom";
import { render, waitFor, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { createMemoryHistory } from 'history';
import userEvent from '@testing-library/user-event';
import TurnConfirmation from '../TurnConfirmation';


const turnNumber = "A1";


test('on render it shows a form to request a new turn', async () => {
    // arrange
    const history = createMemoryHistory()
    history.push(`turns/${turnNumber}/confirmation/`, { turnNumber })

    // act
    render(
        <Router history={history}>
            <TurnConfirmation />
        </Router>
    )

    // assert
    expect(screen.getByText(/Confirmation/i)).toBeInTheDocument();
    expect(screen
        .getByText(/Your turn is number ([a-z]|[A-Z])\d+, we will send you a SMS when you can come back/i))
        .toBeInTheDocument();
    expect(screen.getByText('Ok')).toBeInTheDocument();
})

test('when form is submitted it redirects back to turn request', async () => {
    // arrange
    const history = createMemoryHistory();
    history.push(`turns/${turnNumber}/confirmation/`, { turnNumber });

    render(
        <Router history={history}>
            <TurnConfirmation />
        </Router>
    )
    expect(screen.getByText(/Confirmation/i)).toBeInTheDocument();
    expect(screen
        .getByText(/Your turn is number ([a-z]|[A-Z])\d+, we will send you a SMS when you can come back/i))
        .toBeInTheDocument();
    expect(screen.getByText('Ok')).toBeInTheDocument();

    // act
    let button = screen.getByText('Ok');
    expect(button).toBeInTheDocument();
    userEvent.click(button);

    // assert
    await waitFor(() => expect(history.length).toBe(3));
    await waitFor(() => expect(history.location.pathname).toBe('/turns'));
})
