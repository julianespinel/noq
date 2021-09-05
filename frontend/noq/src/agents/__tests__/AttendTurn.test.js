import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'


import CallNextTurn from '../CallNextTurn'

// API response fixtures
import turn from './callNextTurnResponse.json'
import startTurnResponse from './startTurnResponse.json'
import AttendTurn from '../AttendTurn';

const branchId = 1
const turnId = 1
const baseUrl = "http://localhost:8000/api";

const server = setupServer(
    rest.put(`${baseUrl}/turns/${turnId}`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(startTurnResponse)
        )
    }),
)

beforeAll(() => {
    server.listen();
    localStorage.setItem("branchId", branchId);
    localStorage.setItem("role", "agent");
})

afterEach(() => server.resetHandlers())

afterAll(() => server.close())

test('on first render, it shows the current turn and start button is visible', async () => {
    // arrange
    const history = createMemoryHistory()
    history.push(`agent/turns/${turn.turnNumber}`, { turn })

    // act
    render(
        <Router history={history}>
            <AttendTurn />
        </Router>
    )

    // assert
    expect(screen.getByText(/Attending turn/i)).toBeInTheDocument()
    // Start should be visible, End should not.
    expect(screen.getByText('Start')).not.toHaveClass('d-none');
    expect(screen.getByText('End')).toHaveClass('d-none');
})

test('when the start button is clicked it should get hidden and show the end button', async () => {
    // arrange
    const history = createMemoryHistory();
    history.push(`agent/turns/${turn.turnNumber}`, { turn })
    render(
        <Router history={history}>
            <AttendTurn />
        </Router>
    )

    expect(screen.getByText(/Attending turn/i)).toBeInTheDocument();
    // Start should be visible, End should not.
    expect(screen.getByText('Start')).not.toHaveClass('d-none');
    expect(screen.getByText('End')).toHaveClass('d-none');

    // act
    let startButton = screen.getByText('Start');
    expect(startButton).toBeInTheDocument();
    userEvent.click(startButton);

    // assert
    await waitFor(() => expect(screen.getByText(/Attending turn/i)).toBeInTheDocument());
    // Start should not be visible, End should.
    await waitFor(() => expect(screen.getByText('Start')).toHaveClass('d-none'));
    await waitFor(() => expect(screen.getByText('End')).not.toHaveClass('d-none'));
})

test('when the end button is clicked it should redirect to /agent', async () => {
    // arrange
    const history = createMemoryHistory();
    history.push(`agent/turns/${turn.turnNumber}`, { turn });
    render(
        <Router history={history}>
            <AttendTurn />
        </Router>
    )

    // Click start button to make End button visible
    let startButton = screen.getByText('Start');
    expect(startButton).toBeInTheDocument();
    userEvent.click(startButton);

    // act
    let endButton = screen.getByText('End');
    expect(endButton).toBeInTheDocument();
    userEvent.click(endButton);

    // assert
    await waitFor(() => expect(history.length).toBe(2));
    await waitFor(() => expect(history.location.pathname).toBe('/agent'));
})
