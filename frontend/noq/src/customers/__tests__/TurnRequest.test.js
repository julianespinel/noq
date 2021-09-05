import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import selectEvent from 'react-select-event'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'
import TurnRequest from '../TurnRequest';


// API response fixtures
import getQueuesResponse from './getQueuesResponse.json'
import turnRequestResponse from './turnRequestResponse.json'


const baseUrl = "http://localhost:8000/api";
const branchId = 1;

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

    rest.post(`${baseUrl}/turns`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(turnRequestResponse)
        )
    }),
)

beforeAll(() => {
    server.listen();
    localStorage.setItem("branchId", branchId);
})

afterEach(() => server.resetHandlers())

afterAll(() => server.close())

test('on render it shows a form to request a new turn', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    const { getByTestId, } = render(
        <Router history={history}>
            <TurnRequest />
        </Router>
    )

    // assert
    expect(screen.getByText(/Request turn/i)).toBeInTheDocument();

    const phoneNumberInput = screen.getByText('Phone number');
    expect(phoneNumberInput).not.toHaveValue();

    expect(getByTestId('form')).toHaveFormValues("") // empty select

    expect(screen.getByText('Take turn')).toBeInTheDocument();
})

test('after requesting a new turn, it redirects to turn confirmation', async () => {
    // arrange
    const history = createMemoryHistory();
    const { getByTestId, } = render(
        <Router history={history}>
            <TurnRequest />
        </Router>
    )

    expect(screen.getByText(/Request turn/i)).toBeInTheDocument();
    await selectEvent.select(screen.getByLabelText('select'), ['Payments']);
    expect(getByTestId('form')).toHaveFormValues({ select: '1' });

    userEvent.type(screen.getByText('Phone number'), '+3052551082')

    // act
    let button = screen.getByText('Take turn');
    expect(button).toBeInTheDocument();
    userEvent.click(button);

    // assert
    await waitFor(() => expect(history.length).toBe(2));
    await waitFor(() => expect(history.location.pathname).toBe('/turns/A1/confirmation'));
})
