import React from 'react'

import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { createMemoryHistory } from 'history'

import App from '../../App';


test('loads login', async () => {
    // arrange
    const history = createMemoryHistory()

    // act
    render(<App />)

    // assert
    expect(screen.getByText(/Please log in/i)).toBeInTheDocument()
})
