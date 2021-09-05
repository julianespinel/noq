import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import selectEvent from 'react-select-event'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'


import CallNextTurn from '../CallNextTurn'

// API response fixtures
import getQueuesResponse from './getQueuesReponse.json'
import callNextTurnResponse from './callNextTurnResponse.json'

const branchId = 1
const queueId = 1
const baseUrl = "http://localhost:8000/api";

const server = setupServer(
    rest.get(`${baseUrl}/queues`, (req, res, ctx) => {
        const query = req.url.searchParams;
        const branchId = query.get("branchId");
        const page = query.get("page");
        return res(
            ctx.status(200),
            ctx.json(getQueuesResponse)
        )
    }),

    rest.put(`${baseUrl}/turns`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(callNextTurnResponse)
        )
    }),
)

beforeAll(() => {
    server.listen();
    localStorage.setItem("branchId", branchId);
})

afterEach(() => server.resetHandlers())

afterAll(() => server.close())

test('when the view is rendered it shows the queues of the branch', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    const { getByTestId, } = render(
        <Router history={history}>
            <CallNextTurn />
        </Router>
    )

    // assert
    expect(screen.getByText(/Call next turn/i)).toBeInTheDocument()
    expect(getByTestId('form')).toHaveFormValues("") // empty select

    await selectEvent.select(screen.getByLabelText('select'), ['Payments'])
    expect(getByTestId('form')).toHaveFormValues({ select: '1' })
})

test('when the form is submitted it should transition to attend turn', async () => {
    // arrange
    const history = createMemoryHistory();
    const { getByTestId, } = render(
        <Router history={history}>
            <CallNextTurn />
        </Router>
    )

    expect(screen.getByText(/Call next turn/i)).toBeInTheDocument()
    await selectEvent.select(screen.getByLabelText('select'), ['Payments'])
    expect(getByTestId('form')).toHaveFormValues({ select: '1' })

    // act
    let button = screen.getByText('Next turn');
    expect(button).toBeInTheDocument();
    userEvent.click(button);

    // assert
    await waitFor(() => expect(history.length).toBe(2));
    await waitFor(() => expect(history.location.pathname).toBe('/agent/turns/A1'));
})
