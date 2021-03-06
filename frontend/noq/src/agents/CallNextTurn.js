import React from "react";

import { toast } from 'react-toastify';

import { withRouter } from 'react-router-dom';
import { getQueuesFromBranch, callNextTurn } from "../infrastructure/ApiClient";

import Select from 'react-select';

class CallNextTurn extends React.Component {

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
        this._isMounted = true;

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

        if (this._isMounted) {
            this.setState({ queues: queues });
        }
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(selectedQueue) {
        this.setState({
            selectedQueue: selectedQueue
        });
    }

    async handleSubmit(event) {
        event.preventDefault();

        if (!this.state.selectedQueue) {
            const selectorError = "Please select a queue";
            this.setState({ error: selectorError });
            toast.error(selectorError);
            return;
        }

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
                <form data-testid="form" onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Call next turn</h1>

                    {/*
                        We need this lable to be able to test the <Select> component.
                        That's why it is hidden.
                        See: https://testing-library.com/docs/ecosystem-react-select-event/
                    */}
                    <label className="d-none" htmlFor="select">select</label>
                    <Select inputId="select" name="select" options={this.state.queues}
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

export default withRouter(CallNextTurn);