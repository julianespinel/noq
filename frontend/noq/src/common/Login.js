import React from "react";

import { login } from "../infrastructure/ApiClient";

import { withRouter } from 'react-router-dom';
import { toast } from 'react-toastify';

class Login extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            username: "",
            password: "",
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        // Why event.preventDefault()? : https://www.robinwieruch.de/react-preventdefault

        // Map defining the first tab to show per role.
        const nextTabs = {
            "admin": "companies",
            "manager": "branches",
            "agent": "agent",
            "customer": "turns",
        };

        /*
        TODO: login against your authentication system
            If success ->
                1. Save role, companyId, and branch in the local storage
                2. Get the next tab to navigate to, using the `nextTabs` map
                3. Navigate to the next tab
            Else -> show error
         */

        try {
            const response = login(this.state.username, this.state.password);
            const role = response.role
            localStorage.setItem("role", role);
            localStorage.setItem("companyId", response.companyId);
            localStorage.setItem("branchId", response.branchId);

            const nextTab = nextTabs[role];
            if (nextTab) {
                this.props.history.push(`/${nextTab}`);
                return;
            }
        } catch (error) {
            toast.error(error);
        }
    }

    handleChange(event) {
        this.setState({
            [event.target.id]: event.target.value
        });
    }

    render() {
        return (
            <main className="form-signin">
                <form onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Please log in</h1>

                    <div className="form-floating">
                        <input type="text" className="form-control" id="username"
                            onChange={this.handleChange}
                            placeholder="Username" autoFocus />
                        <label htmlFor="username">Username</label>
                    </div>
                    <div className="form-floating">
                        <input type="password" className="form-control" id="password"
                            onChange={this.handleChange}
                            placeholder="Password" />
                        <label htmlFor="password">Password</label>
                    </div>

                    <div className="checkbox mb-3">
                        {/* Leave some space */}
                    </div>

                    <button className="w-100 btn btn-lg btn-primary" type="submit">Log in</button>
                </form>
            </main>
        );
    }
}

export default withRouter(Login);
