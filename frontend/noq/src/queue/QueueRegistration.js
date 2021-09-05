import React from "react";

import { createQueue } from "../infrastructure/ApiClient";

import { withRouter } from 'react-router-dom';
import { toast } from "react-toastify";

class QueueRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            name: "",
            initialTurn: "",
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    async handleSubmit(event) {
        event.preventDefault();

        const branchId = localStorage.getItem("branchId");
        const [error, queueId] = await createQueue(this.state.name, this.state.initialTurn, branchId);

        if (error) {
            console.error(`Error creating a queue: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        const successMessage = "The queue was created";
        console.log(successMessage);
        toast.success(successMessage);
        localStorage.setItem("queueId", queueId);
        this.setState({
            name: "",
            initialTurn: "",
        });
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
                    <h1 className="h3 mb-3 fw-normal">Add queue</h1>

                    <div className="form-floating">
                        <input type="text" className="form-control" id="name"
                            onChange={this.handleChange} value={this.state.name}
                            placeholder="Name" autoFocus />
                        <label htmlFor="name">Name</label>
                    </div>
                    <div className="form-floating">
                        <input type="text" className="form-control" id="initialTurn"
                            onChange={this.handleChange} value={this.state.initialTurn}
                            placeholder="Initial turn" />
                        <label htmlFor="initialTurn">Initial turn</label>
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

export default withRouter(QueueRegistration);
