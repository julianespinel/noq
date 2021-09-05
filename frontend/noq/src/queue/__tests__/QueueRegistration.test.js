import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'
import QueueRegistration from '../QueueRegistration';

// API response fixtures
import createQueueResponse from './createQueueResponse.json'


const baseUrl = "http://localhost:8000/api";
const branchId = 1;

const server = setupServer(
    rest.post(`${baseUrl}/queues`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(createQueueResponse)
        )
    }),
)

beforeAll(() => {
    server.listen();
    localStorage.setItem("branchId", branchId);
})

afterEach(() => server.resetHandlers())

afterAll(() => server.close())

test('on render it shows a form to create a queue', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    render(
        <Router history={history}>
            <QueueRegistration />
        </Router>
    )

    // assert
    expect(screen.getByText('Add queue')).toBeInTheDocument();

    const nameInput = screen.getByText('Name');
    expect(nameInput).toBeInTheDocument();
    expect(nameInput).not.toHaveValue();

    const initialTurnInput = screen.getByText('Initial turn');
    expect(initialTurnInput).toBeInTheDocument();
    expect(initialTurnInput).not.toHaveValue();

    expect(screen.getByText('Add')).toBeInTheDocument();
})

test('on submit it cleans the form fields', async () => {
    // arrange
    const history = createMemoryHistory()

    render(
        <Router history={history}>
            <QueueRegistration />
        </Router>
    )

    userEvent.type(screen.getByText('Name'), 'Queue X');
    userEvent.type(screen.getByText('Initial turn'), 'X1');

    // act
    let addButton = screen.getByText('Add');
    userEvent.click(addButton);

    // assert
    await waitFor(() => expect(history.length).toBe(1)); // don't change history

    // Check form fields are empty again.
    const nameInput = screen.getByText('Name');
    expect(nameInput).not.toHaveValue();

    const initialTurnInput = screen.getByText('Initial turn');
    expect(initialTurnInput).not.toHaveValue();
})
