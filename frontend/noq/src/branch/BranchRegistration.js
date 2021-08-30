import React from "react";

import { createBranch } from "../infrastructure/ApiClient";

import { withRouter } from 'react-router-dom';
import { toast } from 'react-toastify';

class BranchRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            name: "",
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    async handleSubmit(event) {
        event.preventDefault();

        const companyId = localStorage.getItem("companyId");
        const [error, branchId] = await createBranch(this.state.name, companyId);

        if (error) {
            console.error(`Error creating a branch: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        console.log("The branch was created");
        localStorage.setItem("branchId", branchId);
        await this.props.history.push("/queues");
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
                    <h1 className="h3 mb-3 fw-normal">Add branch</h1>

                    <div className="form-floating">
                        <input type="text" className="form-control" id="name"
                            onChange={this.handleChange} value={this.state.name}
                            placeholder="Name" autoFocus />
                        <label htmlFor="name">Name</label>
                    </div>

                    <div className="checkbox mb-3">
                        {/* Leave some space */}
                    </div>

                    <button className="w-100 btn btn-lg btn-primary" type="submit">Add</button>
                </form>
            </main>
        );
    }
}

export default withRouter(BranchRegistration);
