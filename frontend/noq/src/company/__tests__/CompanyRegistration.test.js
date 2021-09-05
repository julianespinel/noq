import React from 'react'

import { Router } from "react-router-dom";
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { render, waitFor, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'
import userEvent from '@testing-library/user-event'
import CompanyRegistration from '../CompanyRegistration';

// API response fixtures
import createCompanyResponse from './createCompanyResponse.json'


const baseUrl = "http://localhost:8000/api";

const server = setupServer(
    rest.post(`${baseUrl}/companies`, (req, res, ctx) => {
        return res(
            ctx.status(200),
            ctx.json(createCompanyResponse)
        )
    }),
)

beforeAll(() => server.listen())

afterEach(() => server.resetHandlers())

afterAll(() => server.close())

test('on render it shows a form to create a new company', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    render(
        <Router history={history}>
            <CompanyRegistration />
        </Router>
    )

    // assert
    expect(screen.getByText('Add company')).toBeInTheDocument();

    const tinInput = screen.getByText('TIN');
    expect(tinInput).toBeInTheDocument();
    expect(tinInput).not.toHaveValue();

    const nameInput = screen.getByText('Name');
    expect(nameInput).toBeInTheDocument();
    expect(nameInput).not.toHaveValue();

    expect(screen.getByText('Add')).toBeInTheDocument();
})

test('on submit it redirects to /branches', async () => {
    // arrange
    const history = createMemoryHistory()

    render(
        <Router history={history}>
            <CompanyRegistration />
        </Router>
    )

    userEvent.type(screen.getByText('TIN'), '123')
    userEvent.type(screen.getByText('Name'), 'Company X')

    // act
    let addButton = screen.getByText('Add');
    userEvent.click(addButton);

    // assert
    await waitFor(() => expect(history.length).toBe(2));
    await waitFor(() => expect(history.location.pathname).toBe('/branches'));
})
