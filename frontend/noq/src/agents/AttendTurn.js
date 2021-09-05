import React from "react";

import { toast } from 'react-toastify';

import { withRouter } from 'react-router-dom';
import { endTurn, startTurn } from "../infrastructure/ApiClient";

class AttendTurn extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            hasStarted: false,
        }

        this.handleStart = this.handleStart.bind(this);
        this.handleEnd = this.handleEnd.bind(this);
    }

    async handleStart(event) {
        event.preventDefault();

        const turn = this.props.location.state.turn;
        const [error, ] = await startTurn(turn.id);

        if (error) {
            console.error(`Error starting turn ${turn.turnNumber}: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        this.setState({
            hasStarted: true
        });
    }

    async handleEnd(event) {
        event.preventDefault();

        const turn = this.props.location.state.turn;
        const [error, ] = await endTurn(turn.id);

        if (error) {
            console.error(`Error ending turn ${turn.turnNumber}: ${error}`);
            this.setState({ error: error });
            toast.error(error);
            return;
        }

        await this.props.history.push(`/agent`);
    }

    render() {
        const showStart = this.state.hasStarted ? "d-none" : "";
        const showEnd = this.state.hasStarted ? "" : "d-none";
        return (
            <main className="form-signin">
                <h1 className="h3 mb-3 fw-normal">Attending turn {this.props.match.params.turnNumber}</h1>
                <button className={`w-100 btn btn-lg btn-primary mt-5 ${showStart}`}
                    onClick={this.handleStart} type="submit">Start</button>
            <button className={`w-100 btn btn-lg btn-primary mt-5 ${showEnd}`}
                    onClick={this.handleEnd} type="submit">End</button>
            </main>
        );
    }
}

export default withRouter(AttendTurn);