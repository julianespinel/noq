import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'
import BranchRegistration from '../BranchRegistration';

// API response fixtures
import createBranchResponse from './createBranchResponse.json'


const branchId = 1
const turnId = 1
const baseUrl = "http://localhost:8000/api";

const server = setupServer(
    rest.post(`${baseUrl}/branches`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(createBranchResponse)
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

test('on render it shows a form to create a new branch', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    render(
        <Router history={history}>
            <BranchRegistration />
        </Router>
    )

    // assert
    expect(screen.getByText('Add branch')).toBeInTheDocument();

    const nameInput = screen.getByText('Name');
    expect(nameInput).toBeInTheDocument();
    expect(nameInput).not.toHaveValue();

    expect(screen.getByText('Add')).toBeInTheDocument();
})

test('on submit it redirects to /queues', async () => {
    // arrange
    const history = createMemoryHistory()

    render(
        <Router history={history}>
            <BranchRegistration />
        </Router>
    )

    userEvent.type(screen.getByText('Name'), 'Mall X')

    // act
    let addButton = screen.getByText('Add');
    userEvent.click(addButton);

    // assert
    await waitFor(() => expect(history.length).toBe(2));
    await waitFor(() => expect(history.location.pathname).toBe('/queues'));
})
