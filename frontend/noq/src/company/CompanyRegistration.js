import React from "react";

import { createCompany } from "../infrastructure/ApiClient";

import { withRouter } from 'react-router-dom';
import { toast } from 'react-toastify';

class CompanyRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tin: "",
            name: "",
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    async handleSubmit(event) {
        event.preventDefault();

        const [error, companyId] = await createCompany(this.state.tin, this.state.name);
        if (error) {
            console.error(`Error creating a company: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        console.log("The company was created");
        localStorage.setItem("companyId", companyId);
        await this.props.history.push("/branches");
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
                    <h1 className="h3 mb-3 fw-normal">Add company</h1>

                    <div className="form-floating">
                        <input type="text" className="form-control" id="tin"
                            value={this.state.tin}
                            placeholder="TIN" onChange={this.handleChange} autoFocus />
                        <label htmlFor="tin">TIN</label>
                    </div>
                    <div className="form-floating">
                        <input type="text" className="form-control" id="name"
                            value={this.state.name}
                            placeholder="Name" onChange={this.handleChange} />
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

export default withRouter(CompanyRegistration);
