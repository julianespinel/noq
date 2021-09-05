import React from "react";

import { toast } from 'react-toastify';

import { withRouter } from 'react-router-dom';
import { getQueuesFromBranch, requestTurn } from "../infrastructure/ApiClient";

import Select from 'react-select';

class TurnRequest extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            queuesPage: 0,
            selectedQueue: null,
            queues: []
        }

        this.handleSelect = this.handleSelect.bind(this);
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

    handleSelect(selectedQueue) {
        this.setState({
            selectedQueue: selectedQueue
        });
    }

    handleChange(event) {
        this.setState({
            [event.target.id]: event.target.value
        });
    }

    async handleSubmit(event) {
        event.preventDefault();

        const queueId = this.state.selectedQueue.value;
        const [error, turn] = await requestTurn(this.state.phoneNumber, queueId);
        if (error) {
            console.error(`Error requesting turn: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        const turnNumber = turn.turnNumber;
        this.props.history.push(`/turns/${turnNumber}/confirmation`, { turnNumber });
    }

    render() {
        return (
            <main className="form-signin">
                <form onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Request turn</h1>

                    <Select id="selectedQueue" options={this.state.queues}
                        onChange={this.handleSelect} />

                    <div className="form-floating">
                        <input type="tel" className="form-control" id="phoneNumber"
                            onChange={this.handleChange}
                            placeholder="Phone number" />
                        <label htmlFor="floatingPhone">Phone number</label>
                    </div>

                    <div className="checkbox mb-3">
                        {/* Leave some space */}
                    </div>

                    <button className="w-100 btn btn-lg btn-primary" type="submit">Take turn</button>
                </form>
            </main>
        );
    }
}

export default withRouter(TurnRequest);