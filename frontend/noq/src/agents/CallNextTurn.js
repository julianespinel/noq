import React from "react";

import { toast } from 'react-toastify';

import { withRouter } from 'react-router-dom';
import { getQueuesFromBranch, callNextTurn } from "../infrastructure/ApiClient";

import Select from 'react-select'

class AgentView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            queuesPage: 0,
            selectedQueue: null,
            queues: []
        }

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    async componentDidMount() {
        const branchId = localStorage.getItem("branchId");
        const [error, page] = await getQueuesFromBranch(branchId, this.state.queuesPage);
        if (error) {
            console.error(`Error fetching branch queues ${branchId}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        const list = page.content;
        const queues = list.map(queue => ({ value: queue.id, label: queue.name }));
        this.setState({ queues: queues });
    }

    handleChange(selectedQueue) {
        this.setState({
            selectedQueue: selectedQueue
        });
    }

    async handleSubmit(event) {
        event.preventDefault();

        const queueId = this.state.selectedQueue.value;
        const [error, turn] = await callNextTurn(queueId);
        if (error) {
            console.error(`Error calling next turn: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        const turnNumber = turn.turnNumber;
        console.info(`The turn ${turnNumber} was called`);
        localStorage.setItem("turnNumber", turnNumber);
        await this.props.history.push(`/agent/turns/${turnNumber}`, { turn });
    }

    render() {
        return (
            <main className="form-signin">
                <form onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Call next turn</h1>

                    <Select options={this.state.queues}
                        onChange={this.handleChange} />

                    <div className="checkbox mb-3">
                        {/* Leave some space */}
                    </div>

                    <button className="w-100 btn btn-lg btn-primary" type="submit">Next turn</button>
                </form>
            </main>
        );
    }
}

export default withRouter(AgentView);