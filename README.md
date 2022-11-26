<h1>
  <p align="center">SpringCloud Bank Application</p>
</h1>


<b>Description:</b> <em>application is designed for bank systems. By using it you have oportunities to create bank accounts and do some 
actions with them. Application allows to make deposite, payment and transfer between bills of accounts. Each transaction
working on bill will be recorded in the data-base, besides of it app notificate about these transactions.</em>

<h2>
  <p align="center">Application structure</p>
</h2>
<p align="center"<a href="https://ibb.co/NWmq4dS"><img src="https://i.ibb.co/VMQGKcm/stracture.png" alt="stracture" border="0"></a></p>

<h2>
  <p align="center">Requests table</p>
</h2>

<table align="center">
	<thead>
		<tr>
			<th>METHOD</th>
			<th>PATH</th>
			<th>OPTION</th>
		</tr>
	</thead>
	<tbody>
		<tr>
      <td colspan="3" align="center"> <b>account-service</b></td>
		</tr>
		<tr>
			<td><strong>POST</strong></td>
			<td>accounts/</td>
			<td>create account</td>
		</tr>
		<tr>
			<td><strong>GET</strong></td>
			<td>accounts/{accountId}</td>
			<td>get account information by id</td>
		</tr>
		<tr>
			<td><strong>PUT</strong></td>
			<td>accounts/{accountId}</td>
			<td>update account</td>
		</tr>
		<tr>
			<td><strong>DELETE</strong></td>
			<td>accounts/{accountId}</td>
			<td>delete account</td>
		</tr>
		<tr>
      <td colspan="3" align="center"><b>bill-service</b></td>
		</tr>
		<tr>
			<td><strong>POST</strong></td>
			<td>bills/</td>
			<td>create bill</td>
		</tr>
		<tr>
			<td><strong>GET</strong></td>
			<td>bills/{ billId}</td>
			<td>get bill information by id</td>
		</tr>
		<tr>
			<td><strong>GET</strong></td>
			<td>bills/account{accountId}</td>
			<td>get bills information by account id</td>
		</tr>
		<tr>
			<td><strong>PUT</strong></td>
			<td>bills/{ billId}</td>
			<td>update bill</td>
		</tr>
		<tr>
			<td><strong>DELETE</strong></td>
			<td>bills/{ billId}</td>
			<td>delete bill</td>
		</tr>
		<tr>
      <td colspan="3" align="center"><b>deposit-service</codeb></td>
		</tr>
		<tr>
			<td><strong>POST</strong></td>
			<td>deposits/</td>
			<td>make deposit</td>
		</tr>
		<tr>
      <td colspan="3" align="center"><b>payment-service</b></td>
		</tr>
		<tr>
			<td><strong>POST</strong></td>
			<td>payments/</td>
			<td>make payment</td>
		</tr>
		<tr>
      <td colspan="3" align="center"><b>transfer-service</b></td>
		</tr>
		<tr>
			<td><strong>POST</strong></td>
			<td>transfers/</td>
			<td>make transfer</td>
		</tr>
	</tbody>
</table>
