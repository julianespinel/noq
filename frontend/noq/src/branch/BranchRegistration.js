import React from "react";

class BranchRegistration extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tin: "",
            name: "",
            error: ""
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    handleSubmit(event) {
        console.log("handleSubmit");
        console.log(`tin: ${this.state.tin}`);
        console.log(`name: ${this.state.name}`);
    }

    handleChange(event) {
        console.log("handleChange", event.target.id);
        this.setState({
            [event.target.id]: event.target.value
        });
    }

    render() {
        return (
            <main className="form-signin">
                <form>
                    <h1 className="h3 mb-3 fw-normal">Branch</h1>

                    <div className="form-floating">
                        <input type="text" className="form-control" id="floatingName"
                               placeholder="Name"/>
                        <label htmlFor="floatingName">Name</label>
                    </div>

                    <div className="checkbox mb-3">
                        {/* Leave some space */}
                    </div>

                    <button className="w-100 btn btn-lg btn-primary" type="submit">Create</button>
                </form>
            </main>
        );
    }
}

export default BranchRegistration
